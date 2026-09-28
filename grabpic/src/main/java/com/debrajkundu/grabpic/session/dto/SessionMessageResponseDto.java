package com.debrajkundu.grabpic.session.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.debrajkundu.grabpic.session.entity.SessionMessageRole;
import com.fasterxml.jackson.annotation.JsonProperty;

public record SessionMessageResponseDto(
    UUID id,
    @JsonProperty("album_session_id") UUID albumSessionId,
    SessionMessageRole role,
    String content,
    @JsonProperty("media_attachments") List<SessionMessageMediaResponseDto> mediaAttachments,
    @JsonProperty("created_at") Instant createdAt,
    @JsonProperty("updated_at") Instant updatedAt
) {
}

