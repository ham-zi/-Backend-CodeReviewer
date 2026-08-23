package com.reviewer.review.model.dto;

public record PrReviewProcessData(
        String ruleContent,
        String gitRepoOwner,
        String gitRepoName,
        Integer pullNumber,
        String generalSystemPrompt,
        String ruleSystemPrompt
) {
}
