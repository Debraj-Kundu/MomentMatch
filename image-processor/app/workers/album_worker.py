from qdrant_client.http.models import ScoredPoint
from app.core.redis import face_search_queue
from app.core.redis import image_search_queue
from app.schema import MediaModel
from app.services.clustering.ClusterAssignment import ClusterAssignment
from app.services.clustering.ClusterItem import ClusterItem
from app.services.clustering.clustering_service import ClusteringService
from app.services.webhook_service import WebhookService
from app.services.qdrant_service import QdrantService
from app.core.MediaProcessingResult import MediaProcessingResult
from app.services.face_service import FaceService
from app.services.image_embedding_service import ImageEmbeddingService
from app.services.hash_service import HashService
from rq.job import Job
from app.core.redis import album_queue
from app.schema.MediaModel import ProcessAlbumRequest
from app.services.downloader_service import DownloaderService
from app.services.image_loader import ImageLoader
from app.schema.AlbumProcessingResult import AlbumProcessingResult
from app.core.logger import log
from app.core.config import settings
import httpx
import time

#TODO: replace dict with dataclass  
def process_album_job(request: dict) -> AlbumProcessingResult:
    album_data = ProcessAlbumRequest.model_validate(request)

    try:
        webhook = WebhookService()
        log("Processing album", payload={"albumId": album_data.albumId})

        album_result: list[MediaProcessingResult] = []

        for media in album_data.media:
            try:
                result = process_media(media)

                album_result.append(result)
            except Exception as e:
                log("Error processing media", payload={"mediaId": media.id}, err=e)
                continue

        log("Album processed", payload={"albumId": album_data.albumId})

        result = AlbumProcessingResult(
            album_id=album_data.albumId,
            results=album_result
        )

        QdrantService.upsert_album_vector(result)
        
        QdrantService.upsert_image_vectors(result)

        # points: list[ClusterItem] = ClusteringService.get_album_faces(result.album_id)
        
        # assignments: list[ClusterAssignment] = ClusteringService.cluster(points)
        
        # QdrantService.update_clusters(assignments)
        
        # TODO - call webhook
        # await webhook.notify("", "SUCCESS")
        return
    except Exception as e:
        log("Error processing album", payload={"albumId": album_data.albumId}, err=e)
        # TODO - call webhook
        # await webhook.notify("", "FAILED", str(e))
        
def process_media(media_data: MediaModel) -> MediaProcessingResult:
    log("Processing media", payload={"mediaId": media_data.id})
    start_time = time.time()

    data = DownloaderService.download_image(
        media_data.objectKey
    )

    loaded = ImageLoader.load(data)

    faces = FaceService.detect(loaded.bgr)

    image_embedding = ImageEmbeddingService.generate(loaded.pil)

    hash_value = HashService.perceptual_hash(loaded.pil)
    
    end_time = time.time()
    processing_time_ms = int((end_time - start_time) * 1000)

    result = MediaProcessingResult(
        media_id=media_data.id,
        hash=hash_value,
        faces=faces,
        image_embedding=image_embedding.embedding,
        processing_time_ms=processing_time_ms,
        success=True
    )

    log("Media processed", payload={"mediaId": media_data.id})

    return result

def get_cluster_id(similar_faces: list[ScoredPoint]):
    cluster_ids = set()
    for face in similar_faces:
        
        cluster_ids.add(face.payload.get('cluster_id'))
    
    return cluster_ids

def face_search_job(request: dict):
    album_data = ProcessAlbumRequest.model_validate(request)
    try:
        primary_media = album_data.primaryMedia
        primary_image_data = DownloaderService.download_image(primary_media.objectKey)
        primary_loaded = ImageLoader.load(primary_image_data)
        detected_faces = FaceService.detect(primary_loaded.bgr)

        if not detected_faces:
            media_ids = []
            log("No face detected in primary media for similarity search", payload={"albumId": album_data.albumId, "mediaId": primary_media.id})
        else:
            primary_face_embedding = detected_faces[0].embedding
            similar_faces: list[ScoredPoint] = QdrantService.search_primary_face(album_data.albumId, primary_face_embedding)
            # primary_cluster_ids = get_cluster_id(similar_faces)

            # cluster_faces = ClusteringService.get_faces_by_cluster_ids(album_data.albumId, primary_cluster_ids)

            media_ids = FaceService.dedupe_media(similar_faces)
            log("Similar faces found", payload={"albumId": album_data.albumId, "similar_faces_media_id": media_ids})

        webhook_payload = {
            "albumId": album_data.albumId,
            "mediaIds": media_ids
        }
        if hasattr(album_data, "sessionId") and album_data.sessionId:
            webhook_payload["sessionId"] = album_data.sessionId
        elif "sessionId" in request:
            webhook_payload["sessionId"] = request["sessionId"]

        try:
            webhook_url = getattr(settings, "backend_webhook_url", "http://localhost:8081/api/v1/webhooks/process")
            httpx.post(webhook_url, json=webhook_payload, timeout=10)
        except Exception as webhook_err:
            log("Error sending face search webhook", payload={"albumId": album_data.albumId}, err=webhook_err)

        return media_ids
    except Exception as e:
        log("Error searching for similar faces", payload={"albumData": album_data.albumId}, err=e)

def image_search_job(request: dict):
    try:
        log("User prompt: ", request["message"])
        
        user_text_query_embedding: list[float] = ImageEmbeddingService.text_embedding(request["message"])
        log("User prompt embedding: ", user_text_query_embedding)
        
        similar_images: list[ScoredPoint] = QdrantService.search_similar_images(request["album_id"], user_text_query_embedding)
        log("User prompt to qdrant search: ", similar_images)
        
        media_ids = FaceService.dedupe_media(similar_images)
        
        log("Finished image_search_job", payload={"albumId": request["album_id"], "similar_image_media_id": media_ids})

        webhook_payload = {
            "albumId": request["album_id"],
            "mediaIds": media_ids,
            "sessionId": request.get("session_id")
        }
        try:
            webhook_url = getattr(settings, "backend_webhook_url", "http://localhost:8081/api/v1/webhooks/process")
            httpx.post(webhook_url, json=webhook_payload, timeout=10)
        except Exception as webhook_err:
            log("Error sending image search webhook", payload={"albumId": request["album_id"]}, err=webhook_err)

        return media_ids
    except Exception as e:
        log("Error searching for similar images", payload={"albumId": request.get("album_id")}, err=e)

def submit_album(request: ProcessAlbumRequest) -> Job:

    return album_queue.enqueue(
        process_album_job,
        request.model_dump(mode="json")
    )

def match_face(request: ProcessAlbumRequest) -> Job:

    return face_search_queue.enqueue(
        face_search_job,
        request.model_dump(mode="json")
    )
    
def image_similarity(request):
    
    return image_search_queue.enqueue(
        image_search_job,
        request
    )
