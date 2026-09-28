package com.debrajkundu.grabpic.media.dto.operation;

import java.util.UUID;

public record FetchMediaDownloadMetadata(
        UUID id,
        String objectKey,
        String contentType,
        Long sizeBytes,
        String originalFilename
) {}
