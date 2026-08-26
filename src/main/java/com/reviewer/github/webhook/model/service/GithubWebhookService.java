package com.reviewer.github.webhook.model.service;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.reviewer.ai.client.AiReviewClient;
import com.reviewer.enums.ReviewTypeRole;
import com.reviewer.exception.common.NotFoundException;
import com.reviewer.github.webhook.model.dto.GithubWebhookResult;
import com.reviewer.github.webhook.model.entity.GithubWebhookDeliveryEntity;
import com.reviewer.github.webhook.model.entity.GithubWebhookDeliveryStatus;
import com.reviewer.github.webhook.model.repository.GithubWebhookDeliveryRepository;
import com.reviewer.project.model.entity.ProjectEntity;
import com.reviewer.project.model.repository.ProjectRepository;
import com.reviewer.review.model.dao.PrSourceRepository;
import com.reviewer.review.model.dao.ReviewRepository;
import com.reviewer.review.model.entity.PrSourceEntity;
import com.reviewer.review.model.entity.ReviewEntity;
import com.reviewer.review.model.service.ReviewAsyncService;
import com.reviewer.system.model.Entity.SystemPromptEntity;
import com.reviewer.system.model.dao.SystemSettingRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@Service
@RequiredArgsConstructor
@Slf4j
public class GithubWebhookService {

    private static final String PULL_REQUEST_EVENT = "pull_request";
    private static final Set<String> REVIEW_ACTIONS = Set.of(
            "opened",
            "reopened",
            "synchronize"
    );

    private final JsonMapper jsonMapper;
    private final ProjectRepository projectRepository;
    private final ReviewRepository reviewRepository;
    private final PrSourceRepository prSourceRepository;
    private final SystemSettingRepository systemSettingRepository;
    private final GithubWebhookDeliveryRepository deliveryRepository;
    private final ReviewAsyncService reviewAsyncService;
    private final AiReviewClient aiReviewClient;

    @Transactional
    public GithubWebhookResult handle(
            String event,
            String deliveryId,
            byte[] payload
    ) {
        if (!PULL_REQUEST_EVENT.equals(event)) {
            return GithubWebhookResult.ignored(
                    "처리 대상이 아닌 GitHub 이벤트입니다: " + event
            );
        }

        JsonNode root = parsePayload(payload);
        String action = requiredText(root, "action");

        if (!REVIEW_ACTIONS.contains(action)) {
            return GithubWebhookResult.ignored(
                    "처리 대상이 아닌 pull_request action입니다: " + action
            );
        }

        JsonNode repository = requiredObject(root, "repository");
        JsonNode owner = requiredObject(repository, "owner");
        JsonNode pullRequest = requiredObject(root, "pull_request");
        JsonNode head = requiredObject(pullRequest, "head");

        String repositoryOwner = requiredText(owner, "login");
        String repositoryName = requiredText(repository, "name");
        String headSha = requiredText(head, "sha");
        int pullNumber = requiredPositiveInteger(root, "number");

        Optional<ProjectEntity> projectOptional =
                projectRepository
                        .findByGitRepoOwnerIgnoreCaseAndGitRepoNameIgnoreCase(
                                repositoryOwner,
                                repositoryName
                        );

        if (projectOptional.isEmpty()) {
            return GithubWebhookResult.ignored(
                    "등록되지 않은 저장소입니다: " +
                    repositoryOwner + "/" + repositoryName
            );
        }

        ProjectEntity project = projectOptional.get();

        Optional<GithubWebhookDeliveryEntity> sameDelivery =
                deliveryRepository.findByDeliveryId(deliveryId);

        if (sameDelivery.isPresent()) {
            GithubWebhookDeliveryEntity existing = sameDelivery.get();

            if (existing.getStatus() == GithubWebhookDeliveryStatus.FAILED) {
                existing.retry();
                schedule(existing.getWebhookDeliveryId());
                return GithubWebhookResult.retryScheduled(
                        existing.getReview().getReviewId()
                );
            }

            return GithubWebhookResult.duplicate(
                    existing.getReview().getReviewId()
            );
        }

        Optional<GithubWebhookDeliveryEntity> sameCommit =
                deliveryRepository
                        .findByProject_ProjectIdAndPullNumberAndHeadSha(
                                project.getProjectId(),
                                pullNumber,
                                headSha
                        );

        if (sameCommit.isPresent()) {
            return GithubWebhookResult.duplicate(
                    sameCommit.get().getReview().getReviewId()
            );
        }

        if (project.getProjectRule() == null) {
            throw new IllegalStateException(
                    "Webhook 리뷰를 시작하려면 프로젝트에 팀 규칙을 적용해야 합니다."
            );
        }

        SystemPromptEntity systemPrompt =
                systemSettingRepository.findById(ReviewTypeRole.PR)
                        .orElseThrow(() -> new NotFoundException(
                                "활성화된 PR 시스템 프롬프트가 없습니다."
                        ))
                        .getSystemPrompt();

        ReviewEntity review = reviewRepository.save(
                ReviewEntity.of(
                        project,
                        ReviewTypeRole.PR,
                        project.getProjectRule(),
                        systemPrompt,
                        aiReviewClient.getModel()
                )
        );

        prSourceRepository.save(
                PrSourceEntity.of(review, pullNumber)
        );

        GithubWebhookDeliveryEntity delivery = deliveryRepository.save(
                GithubWebhookDeliveryEntity.of(
                        deliveryId,
                        project,
                        review,
                        pullNumber,
                        headSha,
                        action
                )
        );

        schedule(delivery.getWebhookDeliveryId());

        log.info(
                "GitHub Webhook PR 리뷰 등록. deliveryId={}, repository={}/{}, pullNumber={}, headSha={}, reviewId={}",
                deliveryId,
                repositoryOwner,
                repositoryName,
                pullNumber,
                headSha,
                review.getReviewId()
        );

        return GithubWebhookResult.accepted(review.getReviewId());
    }

    private JsonNode parsePayload(byte[] payload) {
        try {
            return jsonMapper.readTree(
                    new String(payload, StandardCharsets.UTF_8)
            );
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "GitHub Webhook payload가 올바른 JSON이 아닙니다.",
                    e
            );
        }
    }

    private JsonNode requiredObject(JsonNode parent, String fieldName) {
        JsonNode value = parent == null ? null : parent.get(fieldName);
        if (value == null || !value.isObject()) {
            throw new IllegalArgumentException(
                    "GitHub Webhook payload에 " + fieldName + " 객체가 없습니다."
            );
        }
        return value;
    }

    private String requiredText(JsonNode parent, String fieldName) {
        JsonNode value = parent == null ? null : parent.get(fieldName);
        if (value == null || !value.isString() || value.asText().isBlank()) {
            throw new IllegalArgumentException(
                    "GitHub Webhook payload에 " + fieldName + " 값이 없습니다."
            );
        }
        return value.asText();
    }

    private int requiredPositiveInteger(JsonNode parent, String fieldName) {
        JsonNode value = parent == null ? null : parent.get(fieldName);
        if (value == null || !value.isInt() || value.asInt() <= 0) {
            throw new IllegalArgumentException(
                    "GitHub Webhook payload의 " + fieldName + " 값이 올바르지 않습니다."
            );
        }
        return value.asInt();
    }

    private void schedule(Long webhookDeliveryId) {
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        reviewAsyncService.processWebhookPr(webhookDeliveryId);
                    }
                }
        );
    }
}
