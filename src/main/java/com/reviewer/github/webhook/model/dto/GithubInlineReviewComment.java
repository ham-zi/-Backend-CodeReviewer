package com.reviewer.github.webhook.model.dto;

public record GithubInlineReviewComment(
        String path,
        int line,
        String side,
        String body
) {
}
