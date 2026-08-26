package com.reviewer.github.webhook.model.dto;

public record GithubWebhookResult(
        String status,
        String message,
        Long reviewId
) {
    public static GithubWebhookResult accepted(Long reviewId) {
        return new GithubWebhookResult(
                "ACCEPTED",
                "AI PR 리뷰를 시작했습니다.",
                reviewId
        );
    }

    public static GithubWebhookResult retryScheduled(Long reviewId) {
        return new GithubWebhookResult(
                "RETRY_SCHEDULED",
                "실패한 AI PR 리뷰를 다시 시작했습니다.",
                reviewId
        );
    }

    public static GithubWebhookResult duplicate(Long reviewId) {
        return new GithubWebhookResult(
                "DUPLICATE",
                "이미 처리했거나 처리 중인 PR 커밋입니다.",
                reviewId
        );
    }

    public static GithubWebhookResult ignored(String message) {
        return new GithubWebhookResult(
                "IGNORED",
                message,
                null
        );
    }
}
