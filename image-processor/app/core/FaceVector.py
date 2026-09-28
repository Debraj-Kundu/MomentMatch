from dataclasses import dataclass


@dataclass(slots = True)
class FaceVector:
    vector: list[float]
    media_id: str
    album_id: str
    bbox: list[float]
    hash: str
    face_index: int
    confidence: float
    age: int
    gender: str
    model: str