package com.reviewer.github.model.dto;

import java.time.Instant;

public record PullRequestResponse(
        Integer pullNumber,
        String title,
        String state,
        Boolean draft,
        String author,
        String baseBranch,
        String headBranch,
        String htmlUrl,
        Instant updatedAt
) {

    public static PullRequestResponse from(
            GithubPullRequestResponse pullRequest
    ) {
        return new PullRequestResponse(
                pullRequest.number(),
                pullRequest.title(),
                pullRequest.state(),
                pullRequest.draft(),
                pullRequest.user() != null
                        ? pullRequest.user().login()
                        : null,
                pullRequest.base() != null
                        ? pullRequest.base().ref()
                        : null,
                pullRequest.head() != null
                        ? pullRequest.head().ref()
                        : null,
                pullRequest.htmlUrl(),
                pullRequest.updatedAt()
        );
    }
}
