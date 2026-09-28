package com.debrajkundu.grabpic.media.dto.album;

import java.util.List;
import java.util.UUID;

import com.debrajkundu.grabpic.media.dto.operation.GenerateUploadUrlResponse;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AlbumInitResponseDto {

    UUID albumId;

    List<GenerateUploadUrlResponse> uploadUrls;

}
