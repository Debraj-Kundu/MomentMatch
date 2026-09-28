package com.debrajkundu.grabpic.media.dto.album;

import com.debrajkundu.grabpic.media.dto.media.MediaMetadataDto;
import com.debrajkundu.grabpic.media.entity.AlbumStatus;
import java.util.UUID;

public record AlbumResponseDto(
        UUID id,
        String name,
        UUID ownerId,
        MediaMetadataDto coverMedia,
        AlbumStatus albumStatus,
        Integer totalMedia,
        Integer processedMedia,
        String createdAt
) {
}
