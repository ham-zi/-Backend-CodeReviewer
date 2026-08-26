package com.reviewer.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "github.webhook")
public record GithubWebhookProperties(
        String secret,
        int maxDiffCharacters
) {
    private static final int DEFAULT_MAX_DIFF_CHARACTERS = 120_000;

    public GithubWebhookProperties {
        if (maxDiffCharacters <= 0) {
            maxDiffCharacters = DEFAULT_MAX_DIFF_CHARACTERS;
        }
    }

    public boolean isConfigured() {
        return secret != null && !secret.isBlank();
    }
}
