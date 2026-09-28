package com.debrajkundu.grabpic.webhook.dto;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAlias;

public record ImageSearchResultDto(
    @JsonAlias({"album_id", "albumId"})
    UUID albumId,

    @JsonAlias({"session_id", "sessionId"})
    UUID sessionId,

    @JsonAlias({"similar_faces_media_id", "similarFacesMediaId", "media_ids", "mediaIds"})
    List<UUID> mediaIds
) {
}

