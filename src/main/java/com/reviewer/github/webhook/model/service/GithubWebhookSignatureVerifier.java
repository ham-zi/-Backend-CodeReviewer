package com.reviewer.github.webhook.model.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Component;

import com.reviewer.configuration.GithubWebhookProperties;

@Component
public class GithubWebhookSignatureVerifier {

    private static final String SIGNATURE_PREFIX = "sha256=";
    private static final String HMAC_SHA_256 = "HmacSHA256";

    private final GithubWebhookProperties properties;

    public GithubWebhookSignatureVerifier(GithubWebhookProperties properties) {
        this.properties = properties;
    }

    public boolean isValid(byte[] payload, String signatureHeader) {
        if (!properties.isConfigured() ||
            payload == null ||
            signatureHeader == null ||
            !signatureHeader.startsWith(SIGNATURE_PREFIX)) {
            return false;
        }

        try {
            Mac mac = Mac.getInstance(HMAC_SHA_256);
            mac.init(new SecretKeySpec(
                    properties.secret().getBytes(StandardCharsets.UTF_8),
                    HMAC_SHA_256
            ));

            byte[] expected = mac.doFinal(payload);
            byte[] actual = HexFormat.of().parseHex(
                    signatureHeader.substring(SIGNATURE_PREFIX.length())
            );

            return MessageDigest.isEqual(expected, actual);
        } catch (IllegalArgumentException e) {
            return false;
        } catch (Exception e) {
            throw new IllegalStateException(
                    "GitHub Webhook 서명을 검증할 수 없습니다.",
                    e
            );
        }
    }
}
