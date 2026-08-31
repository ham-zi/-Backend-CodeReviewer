package com.reviewer.github.webhook.model.dto;

import java.util.List;

public record GithubFormattedReview(
        String summary,
        List<GithubInlineReviewComment> inlineComments
) {
}
