package com.debrajkundu.grabpic.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import com.debrajkundu.grabpic.config.properties.ImageProcessorClientProperties;
import com.debrajkundu.grabpic.config.properties.ImageProcessorProperties;

import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;


@Configuration
@EnableConfigurationProperties({
        ImageProcessorProperties.class,
        ImageProcessorClientProperties.class
})
public class WebClientConfig {
    @Bean
    public WebClient webClient(ImageProcessorProperties imageProcessorProperties,ImageProcessorClientProperties imageProcessorClientProperties) {
        // 🏊♂️ Create a ConnectionProvider with our custom pooling settings
        ConnectionProvider connectionProvider = ConnectionProvider.builder("custom")
                .maxConnections(imageProcessorClientProperties.maxConnections())                 // Max connections in the pool
                .pendingAcquireTimeout(imageProcessorClientProperties.acquireTimeout()) // Timeout to get a connection
                .maxIdleTime(imageProcessorClientProperties.idleTime())    // How long a connection can be idle
                .maxLifeTime(imageProcessorClientProperties.lifeTime())    // Max lifetime of a connection
                .build();
        // 🌐 Configure HttpClient with the ConnectionProvider
        HttpClient httpClient = HttpClient.create(connectionProvider)
                .keepAlive(true)                        // Keep connections alive for reuse
                .responseTimeout(imageProcessorClientProperties.responseTimeout()); // Timeout for responses
        // 🏗️ Build WebClient with the configured HttpClient
        return WebClient.builder()
                .baseUrl(imageProcessorProperties.baseUrl())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}