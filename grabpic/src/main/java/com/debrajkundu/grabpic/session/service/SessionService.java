package com.debrajkundu.grabpic.session.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.debrajkundu.grabpic.common.dto.PageRequest;
import com.debrajkundu.grabpic.common.dto.PageResponse;
import com.debrajkundu.grabpic.common.exception.ResourceNotFoundException;
import com.debrajkundu.grabpic.media.dto.album.AlbumProcessingPayload;
import com.debrajkundu.grabpic.media.dto.media.MediaIdAndObjectKey;
import com.debrajkundu.grabpic.media.entity.Album;
import com.debrajkundu.grabpic.media.entity.Media;
import com.debrajkundu.grabpic.media.entity.UploadStatus;
import com.debrajkundu.grabpic.media.repository.AlbumRepository;
import com.debrajkundu.grabpic.media.repository.MediaRepository;
import com.debrajkundu.grabpic.media.service.MediaService;
import com.debrajkundu.grabpic.session.dto.CreateSessionMessageRequestDto;
import com.debrajkundu.grabpic.session.dto.CreateSessionRequestDto;
import com.debrajkundu.grabpic.session.dto.MatchFaceResponseDto;
import com.debrajkundu.grabpic.session.dto.SessionMessageMediaRequestDto;
import com.debrajkundu.grabpic.session.dto.SessionMessageResponseDto;
import com.debrajkundu.grabpic.session.dto.SessionResponseDto;
import com.debrajkundu.grabpic.session.entity.Session;
import com.debrajkundu.grabpic.session.entity.SessionMessage;
import com.debrajkundu.grabpic.session.entity.SessionMessageMedia;
import com.debrajkundu.grabpic.session.entity.SessionMessageRole;
import com.debrajkundu.grabpic.session.mapper.SessionMapper;
import com.debrajkundu.grabpic.session.mapper.SessionMessageMapper;
import com.debrajkundu.grabpic.session.repository.SessionMessageRepository;
import com.debrajkundu.grabpic.session.repository.SessionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j 
public class SessionService implements SessionServiceInterface {

    private final SessionRepository sessionRepository;
    private final AlbumRepository albumRepository;
    private final MediaRepository mediaRepository;
    private final SessionMessageRepository sessionMessageRepository;
    private final SessionMapper sessionMapper;
    private final SessionMessageMapper sessionMessageMapper;
    private  final MediaService mediaService;
    private final WebClient webClient;

    @Override
    @Transactional
    public SessionResponseDto createSession(CreateSessionRequestDto reqBody) {
        Album albumProxy = albumRepository.getReferenceById(reqBody.albumId());
        
        Media primaryMediaProxy = null;
        if (reqBody.primaryMediaId() != null) {
            // Optional: If you need to verify ownership/status of Media, use findById.
            // If it's purely for mapping the FK, use getReferenceById.
            primaryMediaProxy = mediaRepository.getReferenceById(reqBody.primaryMediaId());
        }

        Session session = Session.builder()
            .title(reqBody.title())
            .album(albumProxy)
            .primaryMedia(primaryMediaProxy)
            .build();   
        
        Session savedSession = sessionRepository.save(session);
        return sessionMapper.toDto(savedSession);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SessionResponseDto> getAllSession(UUID albumId, PageRequest pageRequest) {
        Pageable pageable = org.springframework.data.domain.PageRequest.of(
                pageRequest.getPage(),
                pageRequest.getSize(),
                Sort.by(
                        Sort.Direction.fromString(pageRequest.getOrder()),
                        pageRequest.getSortBy()));

        Page<SessionResponseDto> sessions = sessionRepository.findAllSessionByAlbumId(albumId, pageable)
                .map(sessionMapper::toDto);

        return PageResponse.<SessionResponseDto>builder()
                .content(sessions.getContent())
                .page(sessions.getNumber())
                .size(sessions.getSize())
                .totalElements(sessions.getTotalElements())
                .totalPages(sessions.getTotalPages())
                .first(sessions.isFirst())
                .last(sessions.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SessionResponseDto> getSessionById(UUID sessionId) {
        return sessionRepository.findByIdAndIsDeletedFalse(sessionId).map(sessionMapper::toDto);
    }

    @Override
    @Transactional
    public int updatePrimaryMedia(UUID sessionId, UUID primaryMediaId) {
        Media primaryMediaProxy = primaryMediaId != null ? mediaRepository.getReferenceById(primaryMediaId) : null;
        return sessionRepository.updatePrimaryMedia(sessionId, primaryMediaProxy);
    }

    @Override
    @Transactional
    public SessionMessageResponseDto createMessage(UUID sessionId, CreateSessionMessageRequestDto requestDto) {
        Session session = sessionRepository.findByIdAndIsDeletedFalse(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with id: " + sessionId));

        SessionMessageRole role = requestDto.role() != null ? requestDto.role() : SessionMessageRole.USER;

        SessionMessage message = SessionMessage.builder()
                .session(session)
                .role(role)
                .content(requestDto.content())
                .build();

        if (requestDto.mediaAttachments() != null && !requestDto.mediaAttachments().isEmpty()) {
            int autoRank = 0;
            for (SessionMessageMediaRequestDto item : requestDto.mediaAttachments()) {
                Media media = mediaRepository.findById(item.mediaId())
                        .orElseThrow(() -> new ResourceNotFoundException("Media not found with id: " + item.mediaId()));

                int rank = item.rank() != null ? item.rank() : autoRank++;
                SessionMessageMedia attachment = SessionMessageMedia.builder()
                        .sessionMessage(message)
                        .media(media)
                        .rank(rank)
                        .similarityScore(item.similarityScore())
                        .build();
                message.addMediaAttachment(attachment);
            }
        } else if (requestDto.mediaIds() != null && !requestDto.mediaIds().isEmpty()) {
            int autoRank = 0;
            for (UUID mediaId : requestDto.mediaIds()) {
                Media media = mediaRepository.findById(mediaId)
                        .orElseThrow(() -> new ResourceNotFoundException("Media not found with id: " + mediaId));

                SessionMessageMedia attachment = SessionMessageMedia.builder()
                        .sessionMessage(message)
                        .media(media)
                        .rank(autoRank++)
                        .similarityScore(null)
                        .build();
                message.addMediaAttachment(attachment);
            }
        }

        SessionMessage savedMessage = sessionMessageRepository.save(message);
        return sessionMessageMapper.toDto(savedMessage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SessionMessageResponseDto> getSessionMessages(UUID sessionId, PageRequest pageRequest) {
        if (!sessionRepository.existsByIdAndIsDeletedFalse(sessionId)) {
            throw new ResourceNotFoundException("Session not found with id: " + sessionId);
        }

        Pageable pageable = org.springframework.data.domain.PageRequest.of(
                pageRequest.getPage(),
                pageRequest.getSize(),
                Sort.by(
                        Sort.Direction.fromString(pageRequest.getOrder()),
                        pageRequest.getSortBy()));

        Page<SessionMessageResponseDto> messages = sessionMessageRepository
                .findBySessionIdAndIsDeletedFalse(sessionId, pageable)
                .map(sessionMessageMapper::toDto);

        return PageResponse.<SessionMessageResponseDto>builder()
                .content(messages.getContent())
                .page(messages.getNumber())
                .size(messages.getSize())
                .totalElements(messages.getTotalElements())
                .totalPages(messages.getTotalPages())
                .first(messages.isFirst())
                .last(messages.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SessionMessageResponseDto> getSessionMessageHistory(UUID sessionId) {
        if (!sessionRepository.existsByIdAndIsDeletedFalse(sessionId)) {
            throw new ResourceNotFoundException("Session not found with id: " + sessionId);
        }

        return sessionMessageRepository.findBySessionIdAndIsDeletedFalseOrderByCreatedAtAsc(sessionId)
                .stream()
                .map(sessionMessageMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SessionMessageResponseDto> getMessageById(UUID messageId) {
        return sessionMessageRepository.findByIdAndIsDeletedFalse(messageId)
                .map(sessionMessageMapper::toDto);
    }

    @Override
    @Transactional
    public void deleteMessage(UUID messageId) {
        int updated = sessionMessageRepository.softDeleteById(messageId);
        if (updated == 0) {
            throw new ResourceNotFoundException("Message not found with id: " + messageId);
        }
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

    @Override
    @Transactional
    public MatchFaceResponseDto matchPrimaryFace(UUID sessionId, UUID primaryMediaId) {
        Session session = sessionRepository.findByIdAndIsDeletedFalse(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with id: " + sessionId));

        if (primaryMediaId != null) {
            Media mediaProxy = mediaRepository.getReferenceById(primaryMediaId);
            session.setPrimaryMedia(mediaProxy);
            session = sessionRepository.save(session);
        }

        if (session.getPrimaryMedia() == null) {
            throw new ResourceNotFoundException("Session " + sessionId + " does not have a primary media configured");
        }

        if (session.getAlbum() == null) {
            throw new ResourceNotFoundException("Session " + sessionId + " is not associated with an album");
        }

        UUID albumId = session.getAlbum().getId();
        List<MediaIdAndObjectKey> mediaList = mediaService.getMediaIdAndObjectByAlbumIdAndUploadStatusOrderByImageType(
                albumId,
                UploadStatus.READY
        );

        MediaIdAndObjectKey primaryMedia = new MediaIdAndObjectKey(
                session.getPrimaryMedia().getId(),
                session.getPrimaryMedia().getObjectKey()
        );

        AlbumProcessingPayload payload = new AlbumProcessingPayload(
                albumId.toString(),
                primaryMedia,
                mediaList != null ? mediaList : List.of()
        );
        log.info("mera " + payload.toString());
        try {
            MatchFaceResponseDto response = webClient.post()
                    .uri("/match-face")
                    .bodyValue(payload)
                    .retrieve()
                    .onStatus(
                            HttpStatusCode::isError,
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .map(RuntimeException::new))
                    .bodyToMono(MatchFaceResponseDto.class)
                    .block();

            log.info("Face matching submitted to AI service for session {}: {}", sessionId, response);
            return response;
        } catch (WebClientResponseException ex) {
            log.error("AI service returned error on /match-face: {}", ex.getStatusCode());
            throw new RuntimeException("AI service error: " + ex.getMessage());
        } catch (WebClientRequestException ex) {
            log.error("Cannot connect to AI service on /match-face", ex);
            throw new RuntimeException("Cannot connect to AI service: " + ex.getMessage());
        }
    }

    @Override
    @Transactional
    public MatchFaceResponseDto matchPrimaryFace(UUID sessionId) {
        return matchPrimaryFace(sessionId, null);
    }

    @Override
    public MatchFaceResponseDto findSimilarImages(UUID sessionId, String message) {
        Session session = sessionRepository.findByIdAndIsDeletedFalse(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with id: " + sessionId));

        if (session.getAlbum() == null) {
            throw new ResourceNotFoundException("Session " + sessionId + " is not associated with an album");
        }

        Map<String, String> payload = Map.of(
                "album_id", session.getAlbum().getId().toString(),
                "message", message,
                "session_id", sessionId.toString());

        try {
            return webClient.post()
                    .uri("/find-similar")
                    .bodyValue(payload)
                    .retrieve()
                    .onStatus(
                            HttpStatusCode::isError,
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .map(RuntimeException::new))
                    .bodyToMono(MatchFaceResponseDto.class)
                    .block();
        } catch (WebClientResponseException ex) {
            log.error("AI service returned error on /find-similar: {}", ex.getStatusCode());
            throw new RuntimeException("AI service error: " + ex.getMessage());
        } catch (WebClientRequestException ex) {
            log.error("Cannot connect to AI service on /find-similar", ex);
            throw new RuntimeException("Cannot connect to AI service: " + ex.getMessage());
        }
    }
}
