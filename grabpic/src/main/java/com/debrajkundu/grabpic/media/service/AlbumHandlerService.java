package com.debrajkundu.grabpic.media.service;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.debrajkundu.grabpic.media.entity.UploadStatus;
import com.debrajkundu.grabpic.media.entity.ImageType;
import com.debrajkundu.grabpic.media.dto.album.AlbumInitRequestDto;
import com.debrajkundu.grabpic.media.dto.album.AlbumInitResponseDto;
import com.debrajkundu.grabpic.media.dto.album.AlbumProcessingPayload;
import com.debrajkundu.grabpic.media.dto.album.AlbumRequestDto;
import com.debrajkundu.grabpic.media.dto.album.AlbumResponseDto;
import com.debrajkundu.grabpic.media.dto.media.MediaIdAndObjectKey;
import com.debrajkundu.grabpic.media.dto.operation.GenerateUploadUrlRequests;
import com.debrajkundu.grabpic.media.dto.operation.GenerateUploadUrlResponse;
import com.debrajkundu.grabpic.user.entity.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlbumHandlerService {
    private final AlbumServiceInterface albumService;
    private final MediaServiceInterface mediaService;
    private final WebClient webClient;

    @Transactional
    public AlbumInitResponseDto createAlbumWithMedia(User user, AlbumInitRequestDto requestDto) {

        AlbumResponseDto album = albumService.createAlbum(user,
                requestDto.getAlbum() != null ? requestDto.getAlbum() : new AlbumRequestDto());

        GenerateUploadUrlRequests generateUploadUrlRequests = requestDto.getUploads();
        generateUploadUrlRequests.getFiles().forEach(file -> {
            file.setAlbumId(album.id());
            file.setImageTag(ImageType.GALLERY);
        });

        List<GenerateUploadUrlResponse> uploadUrls = mediaService.generateUploadUrl(generateUploadUrlRequests);

        AlbumInitResponseDto response = AlbumInitResponseDto.builder()
                .albumId(album.id())
                .uploadUrls(uploadUrls)
                
                .build();

        return response;
    }

    public void sendAlbumForProcessing(UUID albumId) {
        List<MediaIdAndObjectKey> mediaList = mediaService.getMediaIdAndObjectByAlbumIdAndUploadStatusOrderByImageType(
                albumId,
                UploadStatus.READY);
        /*
        * TODO: fetch the primary media from session and send in AlbumProcessingPayload
        */
        if (mediaList.isEmpty()) {
            log.warn("No ready media found for album {}", albumId);
            return;
        }

        try {
            webClient.post()
                    .uri("/process-album")
                    .bodyValue(new AlbumProcessingPayload(albumId.toString(), mediaList.get(0), mediaList))
                    .retrieve()
                    .onStatus(
                            HttpStatusCode::isError,
                            response -> response.bodyToMono(String.class)
                                    .map(RuntimeException::new))
                    .toBodilessEntity()
                    .block();
            log.info("Album {} submitted to AI service", albumId);

        } catch (WebClientResponseException ex) {

            log.error("AI returned {}", ex.getStatusCode());

            throw new RuntimeException(ex.getMessage());

        } catch (WebClientRequestException ex) {

            log.error("Cannot connect to AI service", ex);

            throw new RuntimeException(ex.getMessage());
        }

    }
}
