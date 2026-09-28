from typing import Optional
from pydantic import BaseModel

class Media(BaseModel):
    id: str
    objectKey: str

class ProcessAlbumRequest(BaseModel):
    albumId: str
    primaryMedia: Media
    media: list[Media] = []
    sessionId: Optional[str] = None
