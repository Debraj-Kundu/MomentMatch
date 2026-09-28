package com.debrajkundu.grabpic.media.dto.album;

import java.util.List;

import com.debrajkundu.grabpic.media.dto.media.MediaIdAndObjectKey;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AlbumProcessingPayload(
    String albumId,
    String sessionId,
    MediaIdAndObjectKey primaryMedia,
    List<MediaIdAndObjectKey> media
) {
    public AlbumProcessingPayload(String albumId, MediaIdAndObjectKey primaryMedia, List<MediaIdAndObjectKey> media) {
        this(albumId, null, primaryMedia, media);
    }

}
