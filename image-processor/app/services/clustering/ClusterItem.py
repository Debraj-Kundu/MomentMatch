from dataclasses import dataclass

@dataclass(slots=True)
class ClusterItem:

    point_id: str

    vector: list[float]

    payload: dict