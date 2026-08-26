package com.reviewer.github.webhook.model.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reviewer.github.webhook.model.entity.GithubWebhookDeliveryEntity;

public interface GithubWebhookDeliveryRepository
        extends JpaRepository<GithubWebhookDeliveryEntity, Long> {

    Optional<GithubWebhookDeliveryEntity> findByDeliveryId(String deliveryId);

    Optional<GithubWebhookDeliveryEntity>
            findByProject_ProjectIdAndPullNumberAndHeadSha(
                    Long projectId,
                    Integer pullNumber,
                    String headSha
            );
}
