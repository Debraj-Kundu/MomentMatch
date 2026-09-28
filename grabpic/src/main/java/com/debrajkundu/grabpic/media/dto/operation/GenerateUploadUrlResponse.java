package com.debrajkundu.grabpic.media.dto.operation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor 
@AllArgsConstructor 
public class GenerateUploadUrlResponse {

    public UUID mediaId;

    public String uploadUrl;

    public String method;

    public long expiresInSeconds;

    public UploadUrlHeaders headers;
}