package com.debrajkundu.grabpic.media.mapper;

import com.debrajkundu.grabpic.media.dto.media.MediaMetadataDto;
import com.debrajkundu.grabpic.media.entity.Media;
import org.springframework.stereotype.Component;

@Component
public class MediaMapper {

    public MediaMetadataDto toDto(Media media, String downloadUrl) {

        return new MediaMetadataDto(
                media.getId(),
                media.getOriginalFilename(),
                media.getContentType(),
                media.getSizeBytes(),
                media.getUploadStatus(),
                media.getCreatedAt().toString(),
                downloadUrl
        );
    }
}