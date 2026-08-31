package com.reviewer.github.model.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.reviewer.github.model.dto.GithubFileResponse;
import com.reviewer.github.model.dto.GithubPullRequestResponse;
import com.reviewer.github.webhook.model.dto.GithubCommentRequest;
import com.reviewer.github.webhook.model.dto.GithubInlineReviewComment;
import com.reviewer.github.webhook.model.dto.GithubPullRequestReviewRequest;

import tools.jackson.databind.JsonNode;

@Component
public class GithubClient {

    private static final int PR_PAGE_SIZE = 100;
    private static final int PR_FILE_PAGE_SIZE = 100;

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

    /**
     * GitHub REST API - List pull requests
     * GET /repos/{owner}/{repo}/pulls
     *
     * 프로젝트 화면에서 사용자가 리뷰할 PR을 선택할 수 있도록
     * 현재 열려 있는 PR 목록을 가져온다.
     *
     * state=open:
     * - 이미 닫히거나 merge된 PR은 리뷰 대상 선택 목록에서 제외한다.
     *
     * sort=updated, direction=desc:
     * - 최근에 변경된 PR부터 보여주기 위해 사용한다.
     *
     * GitHub API는 한 페이지당 최대 100개의 PR을 반환할 수 있으므로
     * 100개 단위로 마지막 페이지까지 조회한다.
     */
    public List<GithubPullRequestResponse> getPullRequests(
            String owner,
            String repository
    ) {

        List<GithubPullRequestResponse> pullRequests = new ArrayList<>();
        int page = 1;

        while (true) {
            GithubPullRequestResponse[] response = restClient.get()
                    .uri(
                            "/repos/{owner}/{repo}/pulls?state=open&sort=updated&direction=desc&per_page={perPage}&page={page}",
                            owner,
                            repository,
                            PR_PAGE_SIZE,
                            page
                    )
                    .retrieve()
                    .body(GithubPullRequestResponse[].class);

            if (response == null || response.length == 0) {
                break;
            }

            pullRequests.addAll(Arrays.asList(response));

            if (response.length < PR_PAGE_SIZE) {
                break;
            }

            page++;
        }

        return pullRequests;
    }

    /**
     * GitHub REST API - List pull requests files
     * GET /repos/{owner}/{repo}/pulls/{pull_number}/files
     *
     * PR 하나에서 실제로 변경된 파일과 각 파일의 patch(diff)를 가져온다.
     * Branch Review에서 base/head를 직접 입력받아 compare API를 호출했던 것과 달리,
     * PR 번호만 알면 GitHub가 PR의 base/head 관계를 이미 알고 있으므로
     * PR에 포함된 변경 파일을 바로 조회할 수 있다.
     *
     * GitHub API는 기본 30개, 최대 100개씩 파일을 반환하므로
     * 100개 단위로 조회하면서 마지막 페이지까지 가져온다.
     */
    public List<GithubFileResponse> getPullRequestFiles(
            String owner,
            String repository,
            Integer pullNumber
    ) {

        List<GithubFileResponse> files = new ArrayList<>();
        int page = 1;

        while (true) {
            GithubFileResponse[] response = restClient.get()
                    .uri(
                            "/repos/{owner}/{repo}/pulls/{pullNumber}/files?per_page={perPage}&page={page}",
                            owner,
                            repository,
                            pullNumber,
                            PR_FILE_PAGE_SIZE,
                            page
                    )
                    .retrieve()
                    .body(GithubFileResponse[].class);

            if (response == null || response.length == 0) {
                break;
            }

            files.addAll(Arrays.asList(response));

            if (response.length < PR_FILE_PAGE_SIZE) {
                break;
            }

            page++;
        }

        return files;
    }

    /**
     * PR 전체에 대한 리뷰 요약을 Conversation 탭의 타임라인 코멘트로 등록한다.
     * Pull Request는 GitHub Issues API의 issue comment endpoint를 함께 사용한다.
     */
    public String createPullRequestComment(
            String owner,
            String repository,
            Integer pullNumber,
            String body
    ) {
        JsonNode response = restClient.post()
                .uri(
                        "/repos/{owner}/{repo}/issues/{pullNumber}/comments",
                        owner,
                        repository,
                        pullNumber
                )
                .body(new GithubCommentRequest(body))
                .retrieve()
                .body(JsonNode.class);

        JsonNode htmlUrl = response == null ? null : response.get("html_url");

        if (htmlUrl == null || !htmlUrl.isString()) {
            throw new IllegalStateException(
                    "GitHub PR 코멘트 생성 응답에 html_url이 없습니다."
            );
        }

        return htmlUrl.asText();
    }

    /**
     * PR diff의 변경 후 라인에 인라인 리뷰 코멘트를 한 번의 review로 등록한다.
     */
    public String createPullRequestReview(
            String owner,
            String repository,
            Integer pullNumber,
            String headSha,
            List<GithubInlineReviewComment> comments
    ) {
        if (comments.isEmpty()) {
            return null;
        }

        JsonNode response = restClient.post()
                .uri(
                        "/repos/{owner}/{repo}/pulls/{pullNumber}/reviews",
                        owner,
                        repository,
                        pullNumber
                )
                .body(new GithubPullRequestReviewRequest(
                        headSha,
                        "COMMENT",
                        comments
                ))
                .retrieve()
                .body(JsonNode.class);

        JsonNode htmlUrl = response == null ? null : response.get("html_url");

        if (htmlUrl == null || !htmlUrl.isString()) {
            throw new IllegalStateException(
                    "GitHub PR 인라인 리뷰 생성 응답에 html_url이 없습니다."
            );
        }

        return htmlUrl.asText();
    }

}
