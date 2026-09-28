from dataclasses import dataclass

@dataclass(slots=True)
class ClusterAssignment:

    point_id: str

    cluster_id: int