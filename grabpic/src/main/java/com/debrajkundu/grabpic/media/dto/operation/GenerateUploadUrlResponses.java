package com.debrajkundu.grabpic.media.dto.operation;

import lombok.Builder;
import lombok.Data;

import java.util.List;

import jakarta.validation.constraints.NotNull;

@Data
@Builder
public class GenerateUploadUrlResponses {
    @NotNull
    public List<GenerateUploadUrlResponse> responses;
}