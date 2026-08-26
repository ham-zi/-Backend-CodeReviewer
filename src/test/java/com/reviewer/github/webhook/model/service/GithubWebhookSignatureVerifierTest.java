package com.reviewer.github.webhook.model.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import com.reviewer.configuration.GithubWebhookProperties;

class GithubWebhookSignatureVerifierTest {

    private static final String SECRET = "It's a Secret to Everybody";
    private static final byte[] PAYLOAD =
            "Hello, World!".getBytes(StandardCharsets.UTF_8);
    private static final String SIGNATURE =
            "sha256=757107ea0eb2509fc211221cce984b8a37570b6d7586c22c46f4379c8b043e17";

    @Test
    void acceptsGithubOfficialTestVector() {
        GithubWebhookSignatureVerifier verifier = verifier(SECRET);

        assertTrue(verifier.isValid(PAYLOAD, SIGNATURE));
    }

    @Test
    void rejectsTamperedPayloadAndMalformedSignature() {
        GithubWebhookSignatureVerifier verifier = verifier(SECRET);

        assertFalse(verifier.isValid(
                "Hello, attacker!".getBytes(StandardCharsets.UTF_8),
                SIGNATURE
        ));
        assertFalse(verifier.isValid(PAYLOAD, "sha256=not-hex"));
        assertFalse(verifier.isValid(PAYLOAD, null));
    }

    @Test
    void rejectsWhenWebhookSecretIsNotConfigured() {
        assertFalse(verifier("").isValid(PAYLOAD, SIGNATURE));
    }

    private GithubWebhookSignatureVerifier verifier(String secret) {
        return new GithubWebhookSignatureVerifier(
                new GithubWebhookProperties(secret, 120_000)
        );
    }
}
