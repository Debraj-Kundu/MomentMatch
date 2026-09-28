from fastapi import FastAPI
from app.api.process import router
from contextlib import asynccontextmanager
from app.services.qdrant_service import QdrantService

@asynccontextmanager
async def lifespan(app: FastAPI):
    QdrantService.ensure_collection()
    yield

app = FastAPI(lifespan = lifespan)

@app.get("/")
def root():
    return {"message": "Hello FastAPI"}


app.include_router(router)
