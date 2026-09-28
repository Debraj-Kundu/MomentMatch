package com.debrajkundu.grabpic.config.properties;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "image-processor-client")
public record ImageProcessorClientProperties(
        int maxConnections,
        Duration acquireTimeout,
        Duration idleTime,
        Duration lifeTime,
        Duration responseTimeout
) {
}