from dataclasses import dataclass


@dataclass(slots=True)
class ImageVector:
    vector: list[float]
    media_id: str
    album_id: str
    hash: str
    model: str