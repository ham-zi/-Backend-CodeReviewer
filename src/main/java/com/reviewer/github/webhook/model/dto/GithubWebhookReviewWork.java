package com.reviewer.github.webhook.model.dto;

import com.reviewer.enums.ReviewStatusRole;

public record GithubWebhookReviewWork(
        Long webhookDeliveryId,
        Long reviewId,
        String repositoryOwner,
        String repositoryName,
        Integer pullNumber,
        String headSha,
        ReviewStatusRole reviewStatus
) {
}
