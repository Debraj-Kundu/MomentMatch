
from dataclasses import dataclass


@dataclass(slots=True)
class FaceEmbedding:

    embedding: list[float]

    bbox: list[float]

    confidence: float

    age: int | None

    gender: int | None