package com.debrajkundu.grabpic.media.task;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.debrajkundu.grabpic.media.dto.media.MediaIdAndObjectKey;
import com.debrajkundu.grabpic.media.entity.Media;
import com.debrajkundu.grabpic.media.entity.UploadStatus;
import com.debrajkundu.grabpic.media.service.MediaService;
import com.debrajkundu.grabpic.storage.dto.StorageMetadata;
import com.debrajkundu.grabpic.storage.service.StorageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class MediaUploadStatusScheduler {

    private final MediaService mediaService;
    private final StorageService storageService;

    @Scheduled(cron = "0 */1 * * * *")
    public void updatePendingUploadStatusToUploaded() {
        Instant oneMinuteAgo = Instant.now().minus(1, ChronoUnit.MINUTES);
        Instant fiveMinutesAgo = Instant.now().minus(5, ChronoUnit.MINUTES);

        List<MediaIdAndObjectKey> pendingMedia = mediaService.getMediaIdAndObjectByUploadStatus(UploadStatus.PENDING,
                fiveMinutesAgo,
                oneMinuteAgo);

        if (pendingMedia.isEmpty()) {
            log.info("No pending uploads found");
            return;
        }
        log.info("Found {} pending uploads", pendingMedia.size());

        List<UUID> mediaIdsToUpdate = new ArrayList<>();

        for (MediaIdAndObjectKey media : pendingMedia) {
            if (storageService.objectExists(media.objectKey())) {
                mediaIdsToUpdate.add(media.id());
            }
        }

        try {
            mediaService.updateUploadStatusById(mediaIdsToUpdate, UploadStatus.UPLOADED);
            log.info("Updated {} pending uploads to uploaded", mediaIdsToUpdate.size());
        } catch (Exception e) {
            log.error("Failed to update upload status", e);
        }
    }

    // If server is down and restarted after some time might be a race condition b/w
    // UPLOADED and FAILED
    @Scheduled(cron = "0 */1 * * * *")
    public void updatePendingUploadStatusToFailed() {
        Instant tenMinuteAgo = Instant.now().minus(10, ChronoUnit.MINUTES);
        Instant fifteenDaysAgo = Instant.now().minus(15, ChronoUnit.DAYS);

        List<MediaIdAndObjectKey> pendingMedia = mediaService.getMediaIdAndObjectByUploadStatus(UploadStatus.PENDING,
                fifteenDaysAgo,
                tenMinuteAgo);

        if (pendingMedia.isEmpty()) {
            log.info("No pending uploads found");
            return;
        }
        log.info("Found {} pending uploads", pendingMedia.size());

        List<UUID> mediaIdsToUpdate = new ArrayList<>();

        for (MediaIdAndObjectKey media : pendingMedia) {
            if (storageService.objectExists(media.objectKey())) {
                mediaIdsToUpdate.add(media.id());
            }
        }

        try {
            mediaService.updateUploadStatusById(mediaIdsToUpdate, UploadStatus.FAILED);
            log.info("Updated {} pending uploads to failed", mediaIdsToUpdate.size());
        } catch (Exception e) {
            log.error("Failed to update upload status", e);
        }
    }

    @Scheduled(cron = "0 */1 * * * *")
    public void markMediaReadyForProcessing() {
        Instant tenMinuteAgo = Instant.now().minus(2, ChronoUnit.MINUTES);
        Instant fifteenMinuteAgo = Instant.now().minus(15, ChronoUnit.MINUTES);
        List<Media> uploadedMedia = mediaService.getMediaByUploadStatus(UploadStatus.UPLOADED, fifteenMinuteAgo,
                tenMinuteAgo);

        if (uploadedMedia.isEmpty()) {
            return;
        }

        log.info("Marking as ready for processing {} uploaded media files", uploadedMedia.size());

        for (Media media : uploadedMedia) {
            try {
                StorageMetadata metadata = storageService.getObjectMetadata(media.getObjectKey());
                mediaService.updateMediaMetadataAndStatus(
                        media.getId(),
                        metadata.getEtag(),
                        metadata.getChecksum(),
                        UploadStatus.READY);
                log.info("Updated metadata for media {}", media.getId());
            } catch (Exception e) {
                log.error("Failed to process metadata for media {}", media.getId(), e);
            }
        }
    }
}
