package com.debrajkundu.grabpic.storage.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StorageMetadata {
    private String etag;
    private String checksum;
}
