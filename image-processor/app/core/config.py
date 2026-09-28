from pydantic_settings import BaseSettings

class Settings(BaseSettings):
    #redis
    redis_host: str = "localhost"
    redis_port: int = 6379
    redis_db: int = 0

    #object store
    minio_endpoint: str = "localhost:9000"
    minio_access_key: str = "admin"
    minio_secret_key: str = "12345678"
    minio_secure: bool = False
    minio_bucket: str = "media-private"

    #vector db 
    qdrant_host: str = "localhost"
    qdrant_port: int = 6333
    
    #worker queue
    album_queue_name: str = "album-processing"

    #backend webhook
    backend_webhook_url: str = "http://localhost:8081/api/v1/webhooks/process"

    class Config:
        env_file = ".env"


settings = Settings()