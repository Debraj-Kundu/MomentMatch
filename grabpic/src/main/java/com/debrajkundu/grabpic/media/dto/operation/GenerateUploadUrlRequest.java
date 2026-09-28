package com.debrajkundu.grabpic.media.dto.operation;

import java.util.UUID;

import com.debrajkundu.grabpic.media.entity.ImageType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GenerateUploadUrlRequest {
    @NotBlank
    public String filename;

    @NotBlank
    public String fileType;

    @NotNull
    public Long sizeBytes;

    @NotNull
    private UUID albumId;

    @NotNull
    private ImageType imageTag;
    
    @NotBlank 
    private String checkSum;
}