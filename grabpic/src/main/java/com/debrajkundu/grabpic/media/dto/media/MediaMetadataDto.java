package com.debrajkundu.grabpic.media.dto.media;

import com.debrajkundu.grabpic.media.entity.UploadStatus;

import java.util.UUID;

public record MediaMetadataDto(
        UUID id,
        String filename,
        String contentType,
        Long sizeBytes,
        UploadStatus status,
        String createdAt,
        String downloadUrl
) {
}
