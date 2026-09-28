from dataclasses import dataclass
from app.core.MediaProcessingResult import MediaProcessingResult

@dataclass(slots=True)
class AlbumProcessingResult:

    album_id: str

    results: list[MediaProcessingResult]