package com.debrajkundu.grabpic.media.repository;

import com.debrajkundu.grabpic.media.dto.media.MediaIdAndObjectKey;
import com.debrajkundu.grabpic.media.dto.operation.FetchMediaDownloadMetadata;
import com.debrajkundu.grabpic.media.entity.Media;
import com.debrajkundu.grabpic.media.entity.UploadStatus;
import com.debrajkundu.grabpic.media.entity.ImageType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MediaRepository extends JpaRepository<Media, UUID> {
    Optional<Media> findByIdAndIsDeletedFalse(UUID id);

    Page<Media> findByOwnerIdAndIsDeletedFalse(UUID ownerId, Pageable pageable);

    Page<Media> findByIsDeletedFalse(Pageable pageable);

    Optional<Media> findByObjectKey(String objectKey);

    @Query("SELECT new com.debrajkundu.grabpic.media.dto.media.MediaIdAndObjectKey(m.id, m.objectKey) FROM Media m WHERE m.uploadStatus = :uploadStatus AND m.createdAt BETWEEN :dateBefore AND :dateAfter")
    List<MediaIdAndObjectKey> findIdAndObjectKeyByUploadStatusAndCreatedAtBeforeAndCreatedAtAfter(
            UploadStatus uploadStatus,
            Instant dateBefore,
            Instant dateAfter);

    @Query("SELECT new com.debrajkundu.grabpic.media.dto.media.MediaIdAndObjectKey(m.id, m.objectKey) FROM Media m WHERE m.album.id = :albumId AND m.uploadStatus = :uploadStatus")
    List<MediaIdAndObjectKey> findIdAndObjectKeyByAlbumIdAndUploadStatus(UUID albumId, UploadStatus uploadStatus);

    @Query("SELECT new com.debrajkundu.grabpic.media.dto.media.MediaIdAndObjectKey(m.id, m.objectKey) FROM Media m WHERE m.album.id = :albumId AND m.uploadStatus = :uploadStatus ORDER BY m.imageType")
    List<MediaIdAndObjectKey> findIdAndObjectKeyByAlbumIdAndUploadStatusOrderByImageType(UUID albumId, UploadStatus uploadStatus);

    @Query("SELECT m FROM Media m WHERE m.uploadStatus = :uploadStatus AND m.createdAt BETWEEN :dateBefore AND :dateAfter")
    List<Media> findByUploadStatusAndCreatedAtBeforeAndCreatedAtAfter(
            UploadStatus uploadStatus,
            Instant dateBefore,
            Instant dateAfter);

    @Modifying
    @Query("UPDATE Media m SET m.uploadStatus = :uploadStatus WHERE m.id IN :ids")
    int updateUploadStatusById(List<UUID> ids, UploadStatus uploadStatus);

    @Query("""
            SELECT
                m.id,
                m.objectKey,
                m.contentType,
                m.sizeBytes,
                m.originalFilename
                FROM Media m
                WHERE m.id IN :mediaIds
                    AND m.isDeleted = false
                    AND m.uploadStatus = :uploadStatus
            """)
    Page<FetchMediaDownloadMetadata> findByIdInAndIsDeletedFalseAndUploadStatus(@Param("mediaIds") List<UUID> mediaIds,
            @Param("uploadStatus") UploadStatus uploadStatus, Pageable pageable);

    @Query("""
            SELECT new com.debrajkundu.grabpic.media.dto.operation.FetchMediaDownloadMetadata(
                m.id, m.objectKey, m.contentType, m.sizeBytes, m.originalFilename)
            FROM Media m
            WHERE m.album.id = :albumId
              AND m.imageType = :imageType
              AND m.uploadStatus IN :uploadStatuses
              AND m.isDeleted = false
            """)
    Page<FetchMediaDownloadMetadata> findGalleryByAlbumId(
            @Param("albumId") UUID albumId,
            @Param("imageType") ImageType imageType,
            @Param("uploadStatuses") List<UploadStatus> uploadStatuses,
            Pageable pageable);
}
