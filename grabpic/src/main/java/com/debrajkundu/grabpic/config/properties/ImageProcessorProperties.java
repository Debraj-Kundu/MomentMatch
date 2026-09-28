package com.debrajkundu.grabpic.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "image-processor")
public record ImageProcessorProperties(
        String baseUrl
) {
}