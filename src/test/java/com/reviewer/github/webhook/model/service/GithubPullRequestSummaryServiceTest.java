package com.reviewer.github.webhook.model.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.reviewer.enums.ReviewStatusRole;
import com.reviewer.github.model.service.GithubClient;
import com.reviewer.github.webhook.model.dto.GithubWebhookReviewWork;

@ExtendWith(MockitoExtension.class)
class GithubPullRequestSummaryServiceTest {

    @Mock
    private GithubClient githubClient;

    @Mock
    private GithubWebhookDeliveryService webhookDeliveryService;

    private GithubPullRequestSummaryService service;

    @BeforeEach
    void setUp() {
        service = new GithubPullRequestSummaryService(
                githubClient,
                webhookDeliveryService
        );
    }

    @Test
    void createsSummaryWhenPullRequestHasNoPreviousComment() {
        GithubWebhookReviewWork work = work(10L);

        when(webhookDeliveryService.findSummaryCommentUrl(1L, 7))
                .thenReturn(Optional.empty());
        when(webhookDeliveryService.isLatestDelivery(10L, 1L, 7))
                .thenReturn(true);
        when(githubClient.createPullRequestComment("owner", "repo", 7, "summary"))
                .thenReturn("https://github.com/owner/repo/issues/7#issuecomment-101");

        String result = service.publish(work, "summary");

        assertEquals(
                "https://github.com/owner/repo/issues/7#issuecomment-101",
                result
        );
        verify(githubClient).createPullRequestComment(
                "owner", "repo", 7, "summary"
        );
        verify(githubClient, never()).updatePullRequestComment(
                "owner", "repo", 101L, "summary"
        );
    }

    @Test
    void updatesPreviousSummaryUsingCommentIdFromStoredUrl() {
        GithubWebhookReviewWork work = work(11L);

        when(webhookDeliveryService.findSummaryCommentUrl(1L, 7))
                .thenReturn(Optional.of(
                        "https://github.com/owner/repo/pull/7#issuecomment-101"
                ));
        when(webhookDeliveryService.isLatestDelivery(11L, 1L, 7))
                .thenReturn(true);
        when(githubClient.updatePullRequestComment(
                "owner", "repo", 101L, "summary"
        )).thenReturn(
                "https://github.com/owner/repo/pull/7#issuecomment-101"
        );

        String result = service.publish(work, "summary");

        assertEquals(
                "https://github.com/owner/repo/pull/7#issuecomment-101",
                result
        );
        verify(githubClient).updatePullRequestComment(
                "owner", "repo", 101L, "summary"
        );
        verify(githubClient, never()).createPullRequestComment(
                "owner", "repo", 7, "summary"
        );
    }

    @Test
    void skipsPublishingWhenNewerDeliveryExists() {
        GithubWebhookReviewWork work = work(10L);

        when(webhookDeliveryService.findSummaryCommentUrl(1L, 7))
                .thenReturn(Optional.empty());
        when(webhookDeliveryService.isLatestDelivery(10L, 1L, 7))
                .thenReturn(false);

        String result = service.publish(work, "summary");

        assertNull(result);
        verify(githubClient, never()).createPullRequestComment(
                "owner", "repo", 7, "summary"
        );
        verify(githubClient, never()).updatePullRequestComment(
                "owner", "repo", 101L, "summary"
        );
    }

    @Test
    void extractsIssueCommentIdFromGithubHtmlUrl() {
        assertEquals(
                Optional.of(123456789L),
                service.extractCommentId(
                        "https://github.com/owner/repo/pull/7#issuecomment-123456789"
                )
        );
    }

    private GithubWebhookReviewWork work(Long deliveryId) {
        return new GithubWebhookReviewWork(
                deliveryId,
                1L,
                20L,
                "owner",
                "repo",
                7,
                "abcdef",
                ReviewStatusRole.COMPLETED
        );
    }
}
