package com.debrajkundu.grabpic.media.service;

import com.debrajkundu.grabpic.common.dto.PageRequest;
import com.debrajkundu.grabpic.common.dto.PageResponse;
import com.debrajkundu.grabpic.common.exception.ResourceNotFoundException;
import com.debrajkundu.grabpic.media.dto.media.MediaIdAndObjectKey;
import com.debrajkundu.grabpic.media.dto.media.MediaMetadataDto;
import com.debrajkundu.grabpic.media.dto.operation.DownloadUrlResponse;
import com.debrajkundu.grabpic.media.dto.operation.FetchMediaDownloadMetadata;
import com.debrajkundu.grabpic.media.dto.operation.GenerateUploadUrlRequest;
import com.debrajkundu.grabpic.media.dto.operation.GenerateUploadUrlRequests;
import com.debrajkundu.grabpic.media.dto.operation.GenerateUploadUrlResponse;
import com.debrajkundu.grabpic.media.dto.operation.UploadUrlHeaders;
import com.debrajkundu.grabpic.media.entity.Album;
import com.debrajkundu.grabpic.media.entity.Media;
import com.debrajkundu.grabpic.media.entity.UploadStatus;
import com.debrajkundu.grabpic.media.entity.ImageType;
import com.debrajkundu.grabpic.media.mapper.MediaMapper;
import com.debrajkundu.grabpic.media.repository.AlbumRepository;
import com.debrajkundu.grabpic.media.repository.MediaRepository;
import com.debrajkundu.grabpic.storage.service.StorageService;
import com.debrajkundu.grabpic.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MediaService implements MediaServiceInterface {
        private final StorageService storageService;
        private final MediaRepository mediaRepository;
        private final AlbumRepository albumRepository;
        private final MediaMapper mediaMapper;

        @Override
        @Transactional
        public List<GenerateUploadUrlResponse> generateUploadUrl(GenerateUploadUrlRequests request) {
                if (request.getFiles() == null || request.getFiles().isEmpty()) {
                        return new ArrayList<GenerateUploadUrlResponse>();
                }

                User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

                UUID albumId = request.getFiles().get(0).getAlbumId();
                Album album = albumRepository.findById(albumId)
                                .orElseThrow(() -> new ResourceNotFoundException("Album not found for id: " + albumId));

                List<Media> mediaList = request.getFiles()
                                .stream()
                                .map((GenerateUploadUrlRequest file) -> {
                                        UUID mediaId = UUID.randomUUID();
                                        String objectKey = "users/" + user.getId() + "/album/" + albumId + '/'
                                                        + mediaId;
                                        return Media.builder()
                                                        .id(mediaId)
                                                        .owner(user)
                                                        .objectKey(objectKey)
                                                        .originalFilename(file.getFilename())
                                                        .contentType(file.getFileType())
                                                        .sizeBytes(file.getSizeBytes())
                                                        .checksum(file.getCheckSum())
                                                        .uploadStatus(UploadStatus.PENDING)
                                                        .imageType(file.getImageTag())
                                                        .album(album)
                                                        .build();
                                })
                                .toList();

                mediaRepository.saveAll(mediaList);

                List<GenerateUploadUrlResponse> uploads = mediaList.stream()
                                .map((Media media) -> GenerateUploadUrlResponse.builder()
                                                .mediaId(media.getId())
                                                .uploadUrl(storageService.generateUploadUrl(
                                                                media.getObjectKey(),
                                                                media.getContentType(),
                                                                media.getChecksum()))
                                                .method("PUT")
                                                .expiresInSeconds(5* 60)
                                                .headers(UploadUrlHeaders.builder()
                                                                .contentType(media.getContentType())
                                                                .checksumCrc32c(media.getChecksum())
                                                                .build())
                                                .build())
                                .toList();

                return uploads;
        }

        @Override
        @Transactional(readOnly = true)
        public MediaMetadataDto getMediaMetadataById(UUID mediaId) {
                Media media = mediaRepository
                                .findByIdAndIsDeletedFalse(mediaId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Media not found for id: " + mediaId.toString()));

                String downloadUrl = storageService.generateDownloadUrl(media.getObjectKey());

                return mediaMapper.toDto(media, downloadUrl);
        }

        // @Override
        // @Transactional(readOnly = true)
        // public PageResponse<MediaMetadataDto> getMediaMeatadataByOwnerId(UUID
        // ownerId, PageRequest pageRequest) {
        // Pageable pageable = org.springframework.data.domain.PageRequest.of(
        // pageRequest.getPage(),
        // pageRequest.getSize(),
        // Sort.by(
        // Sort.Direction.fromString(pageRequest.getOrder()),
        // pageRequest.getSortBy()));

        // Page<MediaMetadataDto> page = mediaRepository
        // .findByOwnerIdAndIsDeletedFalse(ownerId, pageable)
        // .map(mediaMapper::toDto);

        // return PageResponse.<MediaMetadataDto>builder()
        // .content(page.getContent())
        // .page(page.getNumber())
        // .size(page.getSize())
        // .totalElements(page.getTotalElements())
        // .totalPages(page.getTotalPages())
        // .first(page.isFirst())
        // .last(page.isLast())
        // .build();
        // }

        // @Override
        // @Transactional(readOnly = true)
        // public PageResponse<MediaMetadataDto> getAllMediaMeatadata(PageRequest
        // pageRequest) {
        // Pageable pageable = org.springframework.data.domain.PageRequest.of(
        // pageRequest.getPage(),
        // pageRequest.getSize(),
        // Sort.by(
        // Sort.Direction.fromString(pageRequest.getOrder()),
        // pageRequest.getSortBy()));

        // Page<MediaMetadataDto> page = mediaRepository.findAll(pageable)
        // .map(mediaMapper::toDto);

        // return PageResponse.<MediaMetadataDto>builder()
        // .content(page.getContent())
        // .page(page.getNumber())
        // .size(page.getSize())
        // .totalElements(page.getTotalElements())
        // .totalPages(page.getTotalPages())
        // .first(page.isFirst())
        // .last(page.isLast())
        // .build();
        // }

        @Override
        @Transactional(readOnly = true)
        public Optional<Media> getMediaByObjectKey(String objectKey) {
                return mediaRepository.findByObjectKey(objectKey);
        }

        @Override
        @Transactional(readOnly = true)
        public List<MediaIdAndObjectKey> getMediaIdAndObjectByUploadStatus(UploadStatus uploadStatus,
                        Instant uploadedAfter,
                        Instant uploadedBefore) {

                return mediaRepository
                                .findIdAndObjectKeyByUploadStatusAndCreatedAtBeforeAndCreatedAtAfter(
                                                uploadStatus, uploadedAfter, uploadedBefore);
        }

        @Override
        @Transactional(readOnly = true)
        public List<Media> getMediaByUploadStatus(UploadStatus uploadStatus,
                        Instant uploadedAfter,
                        Instant uploadedBefore) {

                return mediaRepository
                                .findByUploadStatusAndCreatedAtBeforeAndCreatedAtAfter(
                                                uploadStatus, uploadedAfter, uploadedBefore);
        }

        // if not UploadStatus provided then default value is Uploaded
        @Override
        @Transactional
        public void updateUploadStatusById(List<UUID> ids, UploadStatus uploadStatus) {
                mediaRepository.updateUploadStatusById(ids, uploadStatus);
        }

        @Override
        @Transactional
        public void updateMediaMetadataAndStatus(UUID mediaId, String etag, String checksum, UploadStatus status) {
                mediaRepository.findByIdAndIsDeletedFalse(mediaId).ifPresent(media -> {
                        media.setEtag(etag);
                        media.setChecksum(checksum);
                        media.setUploadStatus(status);
                        mediaRepository.save(media);
                });
        }

        @Override
        @Transactional(readOnly = true)
        public PageResponse<DownloadUrlResponse> getDownloadUrls(List<UUID> mediaIds, PageRequest pageRequest) {
                Pageable pageable = org.springframework.data.domain.PageRequest.of(
                                pageRequest.getPage(),
                                pageRequest.getSize(),
                                Sort.by(
                                                Sort.Direction.fromString(pageRequest.getOrder()),
                                                pageRequest.getSortBy()));

                Page<FetchMediaDownloadMetadata> page = mediaRepository.findByIdInAndIsDeletedFalseAndUploadStatus(
                                mediaIds,
                                UploadStatus.UPLOADED, pageable);

                List<DownloadUrlResponse> downloadUrls = page.getContent().stream()
                                .map(fetchMediaDownloadMetadata -> {
                                        String downloadUrl = storageService.generateDownloadUrl(
                                                        fetchMediaDownloadMetadata.objectKey());
                                        return DownloadUrlResponse.builder()
                                                        .mediaId(fetchMediaDownloadMetadata.id())
                                                        .downloadUrl(downloadUrl)
                                                        .build();
                                })
                                .toList();

                return PageResponse.<DownloadUrlResponse>builder()
                                .content(downloadUrls)
                                .page(page.getNumber())
                                .size(page.getSize())
                                .totalElements(page.getTotalElements())
                                .totalPages(page.getTotalPages())
                                .first(page.isFirst())
                                .last(page.isLast())
                                .build();

        }

        @Override
        @Transactional(readOnly = true)
        public PageResponse<DownloadUrlResponse> getGalleryDownloadUrls(UUID albumId, PageRequest pageRequest) {
                Pageable pageable = pageRequest.toPageable();
                Page<FetchMediaDownloadMetadata> page = mediaRepository.findGalleryByAlbumId(
                                albumId,
                                ImageType.GALLERY,
                                List.of(UploadStatus.UPLOADED, UploadStatus.READY, UploadStatus.PROCESSING,
                                                UploadStatus.PROCESSED),
                                pageable);

                List<DownloadUrlResponse> downloadUrls = page.getContent().stream()
                                .map(media -> DownloadUrlResponse.builder()
                                                .mediaId(media.id())
                                                .downloadUrl(storageService.generateDownloadUrl(media.objectKey()))
                                                .build())
                                .toList();

                return PageResponse.<DownloadUrlResponse>builder()
                                .content(downloadUrls)
                                .page(page.getNumber())
                                .size(page.getSize())
                                .totalElements(page.getTotalElements())
                                .totalPages(page.getTotalPages())
                                .first(page.isFirst())
                                .last(page.isLast())
                                .build();
        }

        @Override
        public List<MediaIdAndObjectKey> getMediaIdAndObjectByAlbumIdAndUploadStatus(UUID albumId,
                        UploadStatus uploadStatus) {
                return mediaRepository.findIdAndObjectKeyByAlbumIdAndUploadStatus(albumId, uploadStatus);
        }

        @Override
        public List<MediaIdAndObjectKey> getMediaIdAndObjectByAlbumIdAndUploadStatusOrderByImageType(UUID albumId,
                        UploadStatus uploadStatus) {
                return mediaRepository.findIdAndObjectKeyByAlbumIdAndUploadStatusOrderByImageType(albumId,
                                uploadStatus);
        }
}
