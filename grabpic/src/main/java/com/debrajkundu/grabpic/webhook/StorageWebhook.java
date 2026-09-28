package com.debrajkundu.grabpic.webhook;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.debrajkundu.grabpic.media.entity.UploadStatus;
import com.debrajkundu.grabpic.storage.service.StorageService;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/webhooks/minio")
@RequiredArgsConstructor
public class StorageWebhook {

    private final StorageService storageService;

    @PostMapping()
    public ResponseEntity<Void> storageWebhook(@RequestBody JsonNode payload) {

        System.out.println("MinIO webhook received:");
        System.out.println(payload.toPrettyString());

        String objectKey = payload.get("Key").asText();

        System.out.println("Object key: " + objectKey);

        String mediaId = objectKey.substring(objectKey.lastIndexOf('/') + 1);

        System.out.println("Extracted media ID: " + mediaId);

        UUID mediaUuid = UUID.fromString(mediaId);

        System.out.println("UUID: " + mediaUuid);

        this.storageService.setUploadStatus(mediaUuid, UploadStatus.READY);

        return ResponseEntity.ok().build();
    }
}
