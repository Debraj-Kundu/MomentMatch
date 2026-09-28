package com.debrajkundu.grabpic.session.dto;

import java.util.UUID;

import com.debrajkundu.grabpic.media.dto.album.AlbumResponseDto;
import com.debrajkundu.grabpic.media.dto.media.MediaIdAndObjectKey;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionResponseDto {
    private UUID id;
    private String title;
    private MediaIdAndObjectKey primaryMedia;
    private AlbumResponseDto album;
}
