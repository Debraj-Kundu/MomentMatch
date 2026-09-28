package com.debrajkundu.grabpic.media.dto.operation;

import java.util.List;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GenerateUploadUrlRequests {
    @NotNull
    public List<GenerateUploadUrlRequest> files;
}