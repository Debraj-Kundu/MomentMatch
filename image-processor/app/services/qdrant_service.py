
from app.services.clustering.ClusterAssignment import ClusterAssignment
from uuid import uuid4
from qdrant_client.conversions.common_types import PointStruct
from app.schema.AlbumProcessingResult import AlbumProcessingResult
from qdrant_client.http.models import Distance, Record, ScoredPoint
from qdrant_client.http.models import VectorParams
from app.core.qdrant import client
from app.core.FaceVector import FaceVector
from app.core.ImageVector import ImageVector
from app.core.logger import log
from qdrant_client.http import models


class QdrantService:
    FACE_COLLECTION = "faces"
    IMAGE_COLLECTION = "images"

    @classmethod
    def ensure_collection(cls) -> None:
        if not client.collection_exists(cls.FACE_COLLECTION):
            client.create_collection(
                collection_name=cls.FACE_COLLECTION,
                vectors_config=VectorParams(
                    size=512,
                    distance=Distance.COSINE
                )
            )

        if not client.collection_exists(cls.IMAGE_COLLECTION):
            client.create_collection(
                collection_name=cls.IMAGE_COLLECTION,
                vectors_config=VectorParams(
                    size=512,
                    distance=Distance.COSINE
                )
            )

    @classmethod
    def build_vector(cls, album_result: AlbumProcessingResult) -> list[FaceVector]:
        vectors = []
        for media in album_result.results:
            for i, face in enumerate(media.faces):
                vectors.append(
                    FaceVector(
                        vector = face.embedding,
                        media_id = media.media_id,
                        album_id=album_result.album_id,
                        bbox = face.bbox,
                        hash = media.hash,
                        age = face.age,
                        gender=face.gender,
                        confidence=face.confidence,
                        model="buffalo_l",
                        face_index = i
                    )
                )
        return vectors

    @classmethod
    def upsert_album_vector(cls, album_result: AlbumProcessingResult) -> None:
        vectors = cls.build_vector(album_result)

        points = []
        # TODO: Bulk upload
        for face in vectors:
            points.append(
                PointStruct(
                    id = str(uuid4()),
                    vector = face.vector,
                    payload = {
                        "media_id": face.media_id,
                        "album_id": face.album_id,
                        "bbox": face.bbox,
                        "hash": face.hash,
                        "confidence": face.confidence,
                        "age": face.age,
                        "gender": face.gender,
                        "model": face.model,
                        "face_index": face.face_index
                    }
                )
            )
        
        if not points:
            log("No vectors to upsert.")
            return

        operation_info = client.upsert(
            collection_name=cls.FACE_COLLECTION,
            points=points,
        )

        log("Vectors upserted", payload=operation_info)

    @classmethod
    def update_clusters(cls, assignments: list[ClusterAssignment]) -> None:
        for ass in assignments:
            client.set_payload(
                collection_name=cls.FACE_COLLECTION,
                payload={
                    "cluster_id": ass.cluster_id
                },
                points=[ass.point_id]
            )
        
    @classmethod
    def get_media_face(cls, album_id: str, media_id: str) -> list[Record]:
        points, _ = client.scroll(
            collection_name=cls.FACE_COLLECTION,

            scroll_filter=models.Filter(
                must=[
                    models.FieldCondition(
                        key="album_id",
                        match=models.MatchValue(
                            value=album_id
                        )
                    ),
                    models.FieldCondition(
                        key="media_id",
                        match=models.MatchValue(
                            value=media_id
                        )
                    )
                ]
            ),

            with_payload=True,
            with_vectors=True
        )

        return points

    @classmethod
    def get_primary_face_embedding(
        cls,
        album_id: str,
        media_id: str,
        # face_index: int
    ) -> Record | None:
        points, _ = client.scroll(
            collection_name=cls.FACE_COLLECTION,

            scroll_filter=models.Filter(
                must=[
                    models.FieldCondition(
                        key="album_id",
                        match=models.MatchValue(
                            value=album_id
                        )
                    ),

                    models.FieldCondition(
                        key="media_id",
                        match=models.MatchValue(
                            value=media_id
                        )
                    ),

                    # models.FieldCondition(
                    #     key="face_index",
                    #     match=models.MatchValue(
                    #         value=face_index
                    #     )
                    # )
                ]
            ),

            # with_payload=True,
            with_vectors=True,

            limit=1
        )

        if not points:
            return None

        return points[0]

    @classmethod
    def search_similar_faces(
        cls,
        album_id: str,
        vector: list[float],
        limit: int = 100
    ) -> list[ScoredPoint]:
        return client.query_points(

            collection_name=cls.FACE_COLLECTION,

            query=vector,

            query_filter=models.Filter(
                must=[
                    models.FieldCondition(
                        key="album_id",
                        match=models.MatchValue(
                            value=album_id
                        )
                    )
                ]
            ),

            limit=limit,

            with_payload=True
        ).points
    
    @classmethod
    def search_primary_face(
        cls,
        album_id: str,
        primary_embedding: Record | list[float] | None,
        limit: int = 50,
        score_threshold: float = 0.45
    ) -> list[ScoredPoint]:
        if primary_embedding is None:
            log("No primary face embedding found for album search", payload={"albumId": album_id})
            return []

        vector = primary_embedding.vector if hasattr(primary_embedding, "vector") else primary_embedding
        if not vector:
            log("Primary face embedding is empty for album search", payload={"albumId": album_id})
            return []

        search_kwargs = {
            "collection_name": cls.FACE_COLLECTION,
            "query": vector,
            "query_filter": models.Filter(
                must=[
                    models.FieldCondition(
                        key="album_id",
                        match=models.MatchValue(value=album_id)
                    )
                ]
            ),
            "limit": limit,
            "with_payload": True,
        }

        if score_threshold is not None:
            search_kwargs["score_threshold"] = score_threshold

        return client.query_points(**search_kwargs).points
    
    @classmethod
    def build_image_vectors(
        cls,
        album_result: AlbumProcessingResult
    ) -> list[ImageVector]:

        vectors = []

        for media in album_result.results:
            if not media.success:
                continue

            if not media.image_embedding:
                continue

            vectors.append(
                ImageVector(
                    vector=media.image_embedding,
                    media_id=media.media_id,
                    album_id=album_result.album_id,
                    hash=media.hash,
                    model="openai/clip-vit-base-patch32"
                )
            )

        return vectors

    @classmethod
    def upsert_image_vectors(
        cls,
        album_result: AlbumProcessingResult
    ) -> None:

        vectors = cls.build_image_vectors(album_result)

        points = []

        for image in vectors:
            points.append(
                PointStruct(
                    id=image.media_id,
                    vector=image.vector,
                    payload={
                        "media_id": image.media_id,
                        "album_id": image.album_id,
                        "hash": image.hash,
                        "model": image.model
                    }
                )
            )

        if not points:
            log("No image vectors to upsert.")
            return

        operation_info = client.upsert(
            collection_name=cls.IMAGE_COLLECTION,
            points=points
        )

        log(
            "Image vectors upserted",
            payload=operation_info
        )
        
    @classmethod
    def search_similar_images(
        cls,
        album_id: str,
        vector: list[float],
        limit: int = 50,
        score_threshold: float | None = 0.2,
    ) -> list[ScoredPoint]:

        results = client.query_points(
            collection_name=cls.IMAGE_COLLECTION,
            query=vector,
            query_filter=models.Filter(
                must=[
                    models.FieldCondition(
                        key="album_id",
                        match=models.MatchValue(
                            value=album_id
                        )
                    )
                ]
            ),
            limit=limit,
            score_threshold=score_threshold,
            with_payload=True,
        ).points

        for rank, point in enumerate(results, start=1):
            print(
                f"CLIP RESULT #{rank} | "
                f"score={point.score:.4f} | "
                f"media_id={point.payload.get('media_id')} | "
                f"album_id={point.payload.get('album_id')}"
            )

        return results