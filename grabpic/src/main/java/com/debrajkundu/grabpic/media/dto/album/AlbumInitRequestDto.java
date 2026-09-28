package com.debrajkundu.grabpic.media.dto.album;

import com.debrajkundu.grabpic.media.dto.operation.GenerateUploadUrlRequests;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AlbumInitRequestDto {
    AlbumRequestDto album;

    GenerateUploadUrlRequests uploads;
}
