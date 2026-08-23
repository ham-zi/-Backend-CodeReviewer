package com.reviewer.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.ollama")
public record OllamaProperties(
        String baseUrl,
        String model,
        Integer numCtx,
        Double temperature,
        boolean stream
) {
}
