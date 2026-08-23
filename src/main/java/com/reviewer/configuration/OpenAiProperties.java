package com.reviewer.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.openai")
public record OpenAiProperties(
        String baseUrl,
        String apiKey,
        String model
) {
}
