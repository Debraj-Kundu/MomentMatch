from app.core.FaceEmbedding import FaceEmbedding
from app.core.insightface import face_app
import numpy as np

class FaceService:

    @staticmethod
    def detect(image: np.ndarray) -> list[FaceEmbedding]:
        faces = face_app.get(image)
        results = []

        for face in faces:

            results.append(

                FaceEmbedding(
                    embedding=face.embedding.tolist(),
                    bbox=face.bbox.tolist(),
                    confidence=float(face.det_score),
                    age=int(face.age),
                    gender=int(face.gender)
                )

            )

        return results

    @staticmethod
    def dedupe_media(points) -> list[str]:
        media_ids = set()

        for point in points:
            media_id = point.payload.get("media_id")

            if media_id:
                media_ids.add(media_id)

        return list(media_ids)