package com.reviewer.github.webhook.model.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.reviewer.exception.common.NotFoundException;
import com.reviewer.github.webhook.model.dto.GithubWebhookReviewWork;
import com.reviewer.github.webhook.model.entity.GithubWebhookDeliveryEntity;
import com.reviewer.github.webhook.model.repository.GithubWebhookDeliveryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GithubWebhookDeliveryService {

    private final GithubWebhookDeliveryRepository deliveryRepository;

    @Transactional
    public GithubWebhookReviewWork start(Long webhookDeliveryId) {
        GithubWebhookDeliveryEntity delivery = getDelivery(webhookDeliveryId);
        delivery.start();

        return new GithubWebhookReviewWork(
                delivery.getWebhookDeliveryId(),
                delivery.getProject().getProjectId(),
                delivery.getReview().getReviewId(),
                delivery.getProject().getGitRepoOwner(),
                delivery.getProject().getGitRepoName(),
                delivery.getPullNumber(),
                delivery.getHeadSha(),
                delivery.getReview().getStatus()
        );
    }

    @Transactional
    public void complete(Long webhookDeliveryId, String commentUrl) {
        getDelivery(webhookDeliveryId).complete(commentUrl);
    }

    @Transactional
    public void fail(Long webhookDeliveryId, String errorMessage) {
        getDelivery(webhookDeliveryId).fail(errorMessage);
    }

    @Transactional(readOnly = true)
    public Optional<String> findSummaryCommentUrl(
            Long projectId,
            Integer pullNumber
    ) {
        return deliveryRepository
                .findFirstByProject_ProjectIdAndPullNumberAndCommentUrlIsNotNullOrderByUpdatedAtDesc(
                        projectId,
                        pullNumber
                )
                .map(GithubWebhookDeliveryEntity::getCommentUrl);
    }

    @Transactional(readOnly = true)
    public boolean isLatestDelivery(
            Long webhookDeliveryId,
            Long projectId,
            Integer pullNumber
    ) {
        return !deliveryRepository
                .existsByProject_ProjectIdAndPullNumberAndWebhookDeliveryIdGreaterThan(
                        projectId,
                        pullNumber,
                        webhookDeliveryId
                );
    }

    private GithubWebhookDeliveryEntity getDelivery(Long webhookDeliveryId) {
        return deliveryRepository.findById(webhookDeliveryId)
                .orElseThrow(() -> new NotFoundException(
                        "존재하지 않는 GitHub Webhook delivery입니다."
                ));
    }
}
