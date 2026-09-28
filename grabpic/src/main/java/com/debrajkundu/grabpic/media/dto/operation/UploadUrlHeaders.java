package com.debrajkundu.grabpic.media.dto.operation;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@Builder 
@NoArgsConstructor 
@AllArgsConstructor 
public class UploadUrlHeaders {
    @JsonProperty("Content-Type")
    public String contentType;

    @JsonProperty("x-amz-checksum-crc32c")
    public String checksumCrc32c;
}
