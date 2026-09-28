package com.debrajkundu.grabpic.config;

import com.debrajkundu.grabpic.storage.property.StorageProperties;
import io.minio.MinioClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinoConfig {
    @Bean
    public MinioClient minioClient(
            StorageProperties properties
    ) {

        return MinioClient.builder()
                .endpoint(properties.getEndpoint())
                .credentials(
                        properties.getAccessKey(),
                        properties.getSecretKey()
                )
                .build();
    }
}
