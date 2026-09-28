from dataclasses import dataclass


@dataclass(slots=True)
class ImageEmbedding:
    embedding: list[float]