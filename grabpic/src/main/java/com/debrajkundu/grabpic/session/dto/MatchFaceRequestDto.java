package com.debrajkundu.grabpic.session.dto;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MatchFaceRequestDto(
    @JsonProperty("primary_media_id")
    UUID primaryMediaId
) {
}

