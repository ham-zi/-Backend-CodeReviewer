package com.reviewer.github.model.dto;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GithubPullRequestResponse(
        Integer number,
        String title,
        String state,
        Boolean draft,
        @JsonProperty("html_url") String htmlUrl,
        GithubUserResponse user,
        GithubBranchRefResponse head,
        GithubBranchRefResponse base,
        @JsonProperty("updated_at") Instant updatedAt
) {

    public record GithubUserResponse(
            String login
    ) {
    }

    public record GithubBranchRefResponse(
            String ref
    ) {
    }
}
