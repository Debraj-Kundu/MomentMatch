package com.debrajkundu.grabpic.storage.service;

import java.util.UUID;

import com.debrajkundu.grabpic.media.entity.UploadStatus;
import com.debrajkundu.grabpic.storage.dto.StorageMetadata;

public interface StorageService {
    String generateUploadUrl(String objectKey, String contentType, String expectedCheckSum);

    boolean objectExists(String objectKey);

    String generateDownloadUrl(String objectKey);

    StorageMetadata getObjectMetadata(String objectKey);

    void setUploadStatus(UUID mediaId, UploadStatus uploadStatus);
}
