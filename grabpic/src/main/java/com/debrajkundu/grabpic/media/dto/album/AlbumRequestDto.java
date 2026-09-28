package com.debrajkundu.grabpic.media.dto.album;

import com.debrajkundu.grabpic.media.entity.Media;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AlbumRequestDto {
    @NotBlank
    private String name;

    private Media coverMedia;
}
