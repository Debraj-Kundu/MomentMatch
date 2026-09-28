package com.debrajkundu.grabpic.session.dto;

import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SessionMessageMediaResponseDto(
    UUID id,
    @JsonProperty("media_id") UUID mediaId,
    @JsonProperty("download_url") String downloadUrl,
    @JsonProperty("original_filename") String originalFilename,
    @JsonProperty("content_type") String contentType,
    Integer rank,
    @JsonProperty("similarity_score") Double similarityScore,
    @JsonProperty("created_at") Instant createdAt
) {
}

