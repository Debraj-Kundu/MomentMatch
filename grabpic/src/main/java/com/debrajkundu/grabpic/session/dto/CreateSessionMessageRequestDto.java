package com.debrajkundu.grabpic.session.dto;

import java.util.List;
import java.util.UUID;

import com.debrajkundu.grabpic.session.entity.SessionMessageRole;
import com.fasterxml.jackson.annotation.JsonProperty;

public record CreateSessionMessageRequestDto(
    @JsonProperty("role")
    SessionMessageRole role,

    @JsonProperty("content")
    String content,

    @JsonProperty("media_ids")
    List<UUID> mediaIds,

    @JsonProperty("media_attachments")
    List<SessionMessageMediaRequestDto> mediaAttachments
) {
}

