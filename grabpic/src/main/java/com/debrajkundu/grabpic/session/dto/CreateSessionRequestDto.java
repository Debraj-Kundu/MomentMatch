package com.debrajkundu.grabpic.session.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateSessionRequestDto(
    @NotBlank(message = "Title must not be blank")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    String title,

    @NotNull(message = "album_id is required")
    @JsonProperty("album_id") 
    UUID albumId,
    
    @JsonProperty("primary_media_id") UUID primaryMediaId
) {

}
