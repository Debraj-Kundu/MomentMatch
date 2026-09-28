package com.debrajkundu.grabpic.media.controller;

import com.debrajkundu.grabpic.common.dto.PageRequest;
import com.debrajkundu.grabpic.common.dto.PageResponse;
import com.debrajkundu.grabpic.media.dto.media.MediaMetadataDto;
import com.debrajkundu.grabpic.media.dto.operation.DownloadUrlResponse;
import com.debrajkundu.grabpic.media.dto.operation.GenerateUploadUrlRequests;
import com.debrajkundu.grabpic.media.dto.operation.GenerateUploadUrlResponse;
import com.debrajkundu.grabpic.media.service.MediaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/media")
@RequiredArgsConstructor
public class MediaController {
    private final MediaService mediaService;

    @PostMapping("/upload-url")
    public ResponseEntity<List<GenerateUploadUrlResponse>> generateUploadUrl(
            @Valid @RequestBody GenerateUploadUrlRequests request) {
        return ResponseEntity.ok(
                mediaService.generateUploadUrl(request));
    }

    @GetMapping("/metadata/{id}")
    public ResponseEntity<MediaMetadataDto> getMediaMetadataById(
            @PathVariable UUID id) {

        return ResponseEntity.ok(mediaService.getMediaMetadataById(id));

    }

    // @GetMapping("/metadata")
    // public ResponseEntity<PageResponse<MediaMetadataDto>> getMediaMetadataByOwnerId(
    //         @ModelAttribute PageRequest pageRequest) {
    //     User user = (User) SecurityContextHolder
    //             .getContext()
    //             .getAuthentication()
    //             .getPrincipal();

    //     return ResponseEntity.ok(mediaService.getMediaMeatadataByOwnerId(user.getId(), pageRequest));
    // }

    // @GetMapping("/metadata/all")
    // public ResponseEntity<PageResponse<MediaMetadataDto>> getAllMediaMetadata(
    //         @ModelAttribute PageRequest pageRequest) {

    //     return ResponseEntity.ok(mediaService.getAllMediaMeatadata(pageRequest));
    // }

    @PostMapping("download")
    public ResponseEntity<PageResponse<DownloadUrlResponse>> getDownloadUrls(
            @RequestBody List<UUID> mediaIds,
            @ModelAttribute PageRequest pageRequest) {
        return ResponseEntity.ok(mediaService.getDownloadUrls(mediaIds, pageRequest));
    }
}
