package com.debrajkundu.grabpic.storage.service;

import com.debrajkundu.grabpic.media.entity.Media;
import com.debrajkundu.grabpic.media.entity.UploadStatus;
import com.debrajkundu.grabpic.media.repository.MediaRepository;
import com.debrajkundu.grabpic.storage.dto.StorageMetadata;
import com.debrajkundu.grabpic.storage.property.StorageProperties;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.StatObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MinoStorageService implements StorageService {
    private final MinioClient minioClient;
    private final StorageProperties storageProperties;
    private final MediaRepository mediaRepository;

    @Override
    public String generateUploadUrl(String objectKey, String contentType, String expectedCheckSum) {
        Map<String, String> headers = new HashMap<>();
        
        if(contentType != null && !contentType.isBlank())
            headers.put("Content-Type", contentType);
        if(expectedCheckSum != null && !expectedCheckSum.isBlank())
            headers.put("x-amz-checksum-crc32", expectedCheckSum);
        
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.PUT)
                            .bucket(storageProperties.getBucket())
                            .object(objectKey)
                            .expiry(5 * 60)
                            .extraHeaders(headers)
                            .build());
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate upload URL", e);
        }
    }

    @Override
    public boolean objectExists(String objectKey) {
        try {

            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(storageProperties.getBucket())
                            .object(objectKey)
                            .build());

            return true;

        } catch (ErrorResponseException e) {

            if ("NoSuchKey".equals(e.errorResponse().code())) {
                return false;
            }

            throw new RuntimeException(e);

        } catch (Exception e) {

            throw new RuntimeException(e);
        }
    }

    @Override
    public String generateDownloadUrl(String objectKey) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(storageProperties.getBucket())
                            .object(objectKey)
                            .expiry(60 * 60)
                            .build());
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate download URL", e);
        }
    }

    @Override
    public StorageMetadata getObjectMetadata(String objectKey) {
        try {
            var stat = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(storageProperties.getBucket())
                            .object(objectKey)
                            .build());

            String etag = stat.etag();
            if (etag != null && etag.startsWith("\"") && etag.endsWith("\"")) {
                etag = etag.substring(1, etag.length() - 1);
            }

            return StorageMetadata.builder()
                    .etag(etag)
                    .checksum(etag)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get object metadata", e);
        }
    }

    @Override
    @Transactional
    public void setUploadStatus(UUID mediaId, UploadStatus uploadStatus) {
        Optional<Media> mediaOptional =
                mediaRepository.findByIdAndIsDeletedFalse(mediaId);

        System.out.println("Media found: " + mediaOptional.isPresent());

        if (mediaOptional.isEmpty()) {
            return;
        }

        Media media = mediaOptional.get();

        System.out.println("Before: " + media.getUploadStatus());

        media.setUploadStatus(uploadStatus);

        System.out.println("After: " + media.getUploadStatus());

        mediaRepository.save(media);

        System.out.println("Saved!");
    }
}
