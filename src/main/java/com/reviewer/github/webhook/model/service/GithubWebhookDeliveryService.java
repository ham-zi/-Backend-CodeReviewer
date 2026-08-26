package com.reviewer.github.webhook.model.service;

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

    private GithubWebhookDeliveryEntity getDelivery(Long webhookDeliveryId) {
        return deliveryRepository.findById(webhookDeliveryId)
                .orElseThrow(() -> new NotFoundException(
                        "존재하지 않는 GitHub Webhook delivery입니다."
                ));
    }
}
