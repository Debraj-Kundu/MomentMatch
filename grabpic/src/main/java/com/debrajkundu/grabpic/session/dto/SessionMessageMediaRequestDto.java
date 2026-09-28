package com.debrajkundu.grabpic.session.dto;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public record SessionMessageMediaRequestDto(
    @NotNull(message = "media_id is required")
    @JsonProperty("media_id")
    UUID mediaId,

    @JsonProperty("rank")
    Integer rank,

    @JsonProperty("similarity_score")
    Double similarityScore
) {
}

