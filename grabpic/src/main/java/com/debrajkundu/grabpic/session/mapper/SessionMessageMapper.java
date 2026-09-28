package com.debrajkundu.grabpic.session.mapper;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

import com.debrajkundu.grabpic.storage.service.StorageService;
import com.debrajkundu.grabpic.session.dto.SessionMessageMediaResponseDto;
import com.debrajkundu.grabpic.session.dto.SessionMessageResponseDto;
import com.debrajkundu.grabpic.session.entity.SessionMessage;
import com.debrajkundu.grabpic.session.entity.SessionMessageMedia;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SessionMessageMapper {

    private final StorageService storageService;

    public SessionMessageResponseDto toDto(SessionMessage message) {
        if (message == null) {
            return null;
        }

        List<SessionMessageMediaResponseDto> mediaDtos = message.getMediaAttachments() != null
                ? message.getMediaAttachments().stream()
                        .map(this::toMediaDto)
                        .toList()
                : Collections.emptyList();

        return new SessionMessageResponseDto(
                message.getId(),
                message.getSession() != null ? message.getSession().getId() : null,
                message.getRole(),
                message.getContent(),
                mediaDtos,
                message.getCreatedAt(),
                message.getUpdatedAt()
        );
    }

    public SessionMessageMediaResponseDto toMediaDto(SessionMessageMedia mediaAttachment) {
        if (mediaAttachment == null) {
            return null;
        }

        return new SessionMessageMediaResponseDto(
                mediaAttachment.getId(),
                mediaAttachment.getMedia() != null ? mediaAttachment.getMedia().getId() : null,
            mediaAttachment.getMedia() != null
                ? storageService.generateDownloadUrl(mediaAttachment.getMedia().getObjectKey())
                : null,
                mediaAttachment.getMedia() != null ? mediaAttachment.getMedia().getOriginalFilename() : null,
                mediaAttachment.getMedia() != null ? mediaAttachment.getMedia().getContentType() : null,
                mediaAttachment.getRank(),
                mediaAttachment.getSimilarityScore(),
                mediaAttachment.getCreatedAt()
        );
    }
}

