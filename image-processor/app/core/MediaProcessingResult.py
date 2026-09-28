from dataclasses import dataclass

from app.core.FaceEmbedding import FaceEmbedding
from app.core.ImageEmbedding import ImageEmbedding


@dataclass(slots=True)
class MediaProcessingResult:

    media_id: str

    hash: str

    faces: list[FaceEmbedding]

    image_embedding: list[float]

    processing_time_ms: int

    success: bool