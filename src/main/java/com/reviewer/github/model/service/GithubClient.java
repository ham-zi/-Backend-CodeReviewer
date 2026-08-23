package com.reviewer.github.model.service;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.reviewer.github.model.dto.GithubBranchResponse;
import com.reviewer.github.model.dto.GithubCompareResponse;

@Component
public class GithubClient {

    private final RestClient restClient;

    public GithubClient(
            RestClient.Builder builder,
            @Value("${github.token}") String githubToken) {

        this.restClient = builder
                .baseUrl("https://api.github.com")
                .defaultHeader("Authorization", "Bearer " + githubToken)
                .defaultHeader("Accept", "application/vnd.github+json")
                .defaultHeader("X-GitHub-Api-Version", "2026-03-10")
                .build();
    }

    public List<GithubBranchResponse> getBranches(
            String owner,
            String repository) {

        GithubBranchResponse[] response = restClient.get()
                .uri("/repos/{owner}/{repo}/branches",
                        owner,
                        repository)
                .retrieve()
                .body(GithubBranchResponse[].class);

        if (response == null) {
            return List.of();
        }

        return Arrays.asList(response);
    }

    public GithubCompareResponse compare(
            String owner,
            String repository,
            String base,
            String head) {

        return restClient.get()
                .uri("/repos/{owner}/{repo}/compare/{base}...{head}",
                        owner,
                        repository,
                        base,
                        head)
                .retrieve()
                .body(GithubCompareResponse.class);
    }
}