package com.reviewer.github.webhook.model.dto;

import java.util.List;

public record GithubPullRequestReviewRequest(
        String commit_id,
        String event,
        List<GithubInlineReviewComment> comments
) {
}
