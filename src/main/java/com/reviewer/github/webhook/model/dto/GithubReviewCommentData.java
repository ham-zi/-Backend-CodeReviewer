package com.reviewer.github.webhook.model.dto;

public record GithubReviewCommentData(
        Long reviewId,
        String aiModel,
        String generalRawResponse,
        String ruleRawResponse
) {
}
