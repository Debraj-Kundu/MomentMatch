from redis import Redis
from rq import Queue

from app.core.config import settings


redis_client = Redis(
    host=settings.redis_host,
    port=settings.redis_port,
    db=settings.redis_db,
    decode_responses=False
)

album_queue = Queue(
    name="album-processing",
    connection=redis_client
)

face_search_queue = Queue(
    name="face_search_queue",
    connection=redis_client
)


image_search_queue = Queue(
    name="image_search_queue",
    connection=redis_client
)