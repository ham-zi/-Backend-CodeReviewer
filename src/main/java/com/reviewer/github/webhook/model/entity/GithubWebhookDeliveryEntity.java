package com.reviewer.github.webhook.model.entity;

import java.time.Instant;

import com.reviewer.project.model.entity.ProjectEntity;
import com.reviewer.review.model.entity.ReviewEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "GITHUB_WEBHOOK_DELIVERY",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "UK_GWD_DELIVERY_ID",
                        columnNames = "DELIVERY_ID"
                ),
                @UniqueConstraint(
                        name = "UK_GWD_PR_HEAD",
                        columnNames = {
                                "PROJECT_ID",
                                "PULL_NUMBER",
                                "HEAD_SHA"
                        }
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class GithubWebhookDeliveryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long webhookDeliveryId;

    @Column(name = "DELIVERY_ID", nullable = false, length = 100)
    private String deliveryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PROJECT_ID", nullable = false)
    private ProjectEntity project;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REVIEW_ID", nullable = false, unique = true)
    private ReviewEntity review;

    @Column(name = "PULL_NUMBER", nullable = false)
    private Integer pullNumber;

    @Column(name = "HEAD_SHA", nullable = false, length = 64)
    private String headSha;

    @Column(name = "ACTION", nullable = false, length = 30)
    private String action;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 20)
    private GithubWebhookDeliveryStatus status;

    @Column(name = "COMMENT_URL", length = 1000)
    private String commentUrl;

    @Column(name = "ERROR_MESSAGE", length = 1000)
    private String errorMessage;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "UPDATED_AT", nullable = false)
    private Instant updatedAt;

    private GithubWebhookDeliveryEntity(
            String deliveryId,
            ProjectEntity project,
            ReviewEntity review,
            Integer pullNumber,
            String headSha,
            String action
    ) {
        this.deliveryId = deliveryId;
        this.project = project;
        this.review = review;
        this.pullNumber = pullNumber;
        this.headSha = headSha;
        this.action = action;
        this.status = GithubWebhookDeliveryStatus.PENDING;
    }

    public static GithubWebhookDeliveryEntity of(
            String deliveryId,
            ProjectEntity project,
            ReviewEntity review,
            Integer pullNumber,
            String headSha,
            String action
    ) {
        return new GithubWebhookDeliveryEntity(
                deliveryId,
                project,
                review,
                pullNumber,
                headSha,
                action
        );
    }

    public void start() {
        this.status = GithubWebhookDeliveryStatus.PROCESSING;
        this.errorMessage = null;
    }

    public void complete(String commentUrl) {
        this.status = GithubWebhookDeliveryStatus.COMPLETED;
        this.commentUrl = commentUrl;
        this.errorMessage = null;
    }

    public void fail(String errorMessage) {
        this.status = GithubWebhookDeliveryStatus.FAILED;
        this.errorMessage = truncate(errorMessage, 1000);
    }

    public void retry() {
        this.status = GithubWebhookDeliveryStatus.PENDING;
        this.errorMessage = null;
    }

    @PrePersist
    private void onCreated() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    private void onUpdated() {
        this.updatedAt = Instant.now();
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
