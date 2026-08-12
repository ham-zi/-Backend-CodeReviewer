package com.reviewer.review.model.dto;

public record ReviewProcessData(
        String ruleContent,
        String gitRepoOwner,
        String gitRepoName
) {
}