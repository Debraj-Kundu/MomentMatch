
from app.services.clustering.ClusterAssignment import ClusterAssignment
import numpy as np
from numpy import matrix
from app.services.clustering.ClusterItem import ClusterItem
from qdrant_client.http import models
from qdrant_client.http.models import Record
from app.core.qdrant import client
from sklearn.cluster import DBSCAN

class ClusteringService:
    @staticmethod
    def get_album_faces(album_id: str) -> list[ClusterItem]:
        points, next_offset = client.scroll(
            collection_name="faces",
            scroll_filter=models.Filter(
                must=[
                    models.FieldCondition(key="album_id", match=models.MatchValue(value=album_id))
                ]
            ),
            with_payload=True,
            with_vectors=True,
        )
        
        result = []

        for point in points:
            result.append(
                ClusterItem(
                    point_id = point.id,
                    vector = point.vector,
                    payload = point.payload
                )
            )
        
        return result

    @staticmethod
    def cluster(points: list[ClusterItem]) -> list[ClusterAssignment]:
        matrix = np.asarray( [p.vector for p in points] )

        cluster_ids = DBSCAN(eps = 0.55, min_samples=1, metric='cosine').fit_predict(matrix)

        result = []
        for i, point in enumerate(points):
            result.append(
                ClusterAssignment(
                    point_id = point.point_id,
                    cluster_id = int(cluster_ids[i])
                )
            )
        
        return result

    @staticmethod
    def get_faces_by_cluster_ids(
        album_id: str,
        cluster_ids: set[str]
    ) -> list[Record]:
        if not cluster_ids:
            return []

        result = client.scroll(
            collection_name='faces',

            scroll_filter=models.Filter(
                must=[
                    models.FieldCondition(
                        key="album_id",
                        match=models.MatchValue(
                            value=album_id
                        )
                    )
                ],

                should=[
                    models.FieldCondition(
                        key="cluster_id",
                        match=models.MatchValue(
                            value=cluster_id
                        )
                    )
                    for cluster_id in cluster_ids
                ]
            ),

            with_payload=True,
            with_vectors=False
        )
        return result[0]
