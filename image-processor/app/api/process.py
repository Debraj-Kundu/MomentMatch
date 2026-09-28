from fastapi import APIRouter

from app.schema.MediaModel import ProcessAlbumRequest
from app.workers.album_worker import submit_album
from app.workers.album_worker import match_face
from app.workers.album_worker import image_similarity

from app.core.logger import log

router = APIRouter()


@router.post("/process-album", status_code=202)
async def process_album(request: ProcessAlbumRequest):
    log("Received request to process album", payload=request)

    job = submit_album(request)

    return {
        "jobId": job.id,
        "status": "QUEUED"
    }

@router.post("/match-face", status_code=202)
async def match_primary_face(request: ProcessAlbumRequest):
    log("Received request to match face", payload=request)
    
    job = match_face(request)
    
    return {
        "jobId": job.id,
        "status": "QUEUED"
    }
    
@router.post("/find-similar", status_code=202)
async def match_image_face(request: dict):
    log("Received request to find similar image", payload=request)
    
    job = image_similarity(request)
    
    return {
        "jobId": job.id,
        "status": "QUEUED"
    }