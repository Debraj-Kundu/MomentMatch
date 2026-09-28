package com.debrajkundu.grabpic.media.dto.operation;

import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DownloadUrlResponse {

    private UUID mediaId;

    private String downloadUrl;
}
