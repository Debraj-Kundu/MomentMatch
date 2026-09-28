package com.debrajkundu.grabpic.media.service;

import com.debrajkundu.grabpic.common.dto.PageRequest;
import com.debrajkundu.grabpic.common.dto.PageResponse;
import com.debrajkundu.grabpic.media.dto.media.MediaIdAndObjectKey;
import com.debrajkundu.grabpic.media.dto.media.MediaMetadataDto;
import com.debrajkundu.grabpic.media.dto.operation.DownloadUrlResponse;
import com.debrajkundu.grabpic.media.dto.operation.GenerateUploadUrlRequests;
import com.debrajkundu.grabpic.media.dto.operation.GenerateUploadUrlResponse;
import com.debrajkundu.grabpic.media.entity.Media;
import com.debrajkundu.grabpic.media.entity.UploadStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MediaServiceInterface {
        List<GenerateUploadUrlResponse> generateUploadUrl(GenerateUploadUrlRequests request);

        MediaMetadataDto getMediaMetadataById(UUID mediaId);

        // PageResponse<MediaMetadataDto> getMediaMeatadataByOwnerId(UUID ownerId, PageRequest pageRequest);

        // PageResponse<MediaMetadataDto> getAllMediaMeatadata(PageRequest pageRequest);

        Optional<Media> getMediaByObjectKey(String objectKey);

        List<MediaIdAndObjectKey> getMediaIdAndObjectByUploadStatus(UploadStatus uploadStatus, Instant uploadedAfter,
                        Instant uploadedBefore);

        List<Media> getMediaByUploadStatus(UploadStatus uploadStatus, Instant uploadedAfter,
                        Instant uploadedBefore);

        List<MediaIdAndObjectKey> getMediaIdAndObjectByAlbumIdAndUploadStatus(UUID albumId, UploadStatus uploadStatus);
        
        List<MediaIdAndObjectKey> getMediaIdAndObjectByAlbumIdAndUploadStatusOrderByImageType(UUID albumId, UploadStatus uploadStatus);

        void updateUploadStatusById(List<UUID> ids, UploadStatus uploadStatus);

        void updateMediaMetadataAndStatus(UUID mediaId, String etag, String checksum, UploadStatus status);

        PageResponse<DownloadUrlResponse> getDownloadUrls(List<UUID> mediaIds, PageRequest pageRequest);

        PageResponse<DownloadUrlResponse> getGalleryDownloadUrls(UUID albumId, PageRequest pageRequest);
}
