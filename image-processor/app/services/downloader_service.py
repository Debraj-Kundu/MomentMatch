from io import BytesIO
from app.core.object_store import minio_client
from app.core.config import settings


class DownloaderService:

    @staticmethod
    def download_image(object_key: str) -> bytes:

        response = minio_client.get_object(
            settings.minio_bucket,
            object_key
        )

        data = response.read()

        response.close()
        response.release_conn()

        return data