package com.reviewer.github.webhook.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reviewer.api.model.vo.ApiResponse;
import com.reviewer.configuration.GithubWebhookProperties;
import com.reviewer.github.webhook.model.dto.GithubWebhookResult;
import com.reviewer.github.webhook.model.service.GithubWebhookService;
import com.reviewer.github.webhook.model.service.GithubWebhookSignatureVerifier;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/webhooks/github")
public class GithubWebhookController {

    private final GithubWebhookProperties properties;
    private final GithubWebhookSignatureVerifier signatureVerifier;
    private final GithubWebhookService webhookService;

    @PostMapping
    public ResponseEntity<ApiResponse<GithubWebhookResult>> receive(
            @RequestHeader("X-GitHub-Event") String event,
            @RequestHeader("X-GitHub-Delivery") String deliveryId,
            @RequestHeader("X-Hub-Signature-256") String signature,
            @RequestBody byte[] payload
    ) {
        if (!properties.isConfigured()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(ApiResponse.internalServerError(
                            "GITHUB_WEBHOOK_SECRET이 설정되지 않았습니다.",
                            null
                    ));
        }

        if (!signatureVerifier.isValid(payload, signature)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.unAuthorized(
                            "GitHub Webhook 서명이 올바르지 않습니다.",
                            null
                    ));
        }

        try {
            GithubWebhookResult result = webhookService.handle(
                    event,
                    deliveryId,
                    payload
            );

            return ResponseEntity.accepted()
                    .body(ApiResponse.success(
                            "GitHub Webhook을 수신했습니다.",
                            result
                    ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.badRequest(e.getMessage(), null));
        }
    }
}
