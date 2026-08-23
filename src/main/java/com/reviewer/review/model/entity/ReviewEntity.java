package com.reviewer.review.model.entity;

import java.time.Instant;

import com.reviewer.enums.ReviewStatusRole;
import com.reviewer.enums.ReviewTypeRole;
import com.reviewer.project.model.entity.ProjectEntity;
import com.reviewer.project.projectRule.model.entity.ProjectRuleEntity;
import com.reviewer.system.model.Entity.SystemPromptEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "REVIEW")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ReviewEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewId;
    @JoinColumn(name = "PROJECT_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private ProjectEntity project;
    @Enumerated(EnumType.STRING)
    @Column(name = "REVIEW_TYPE",nullable = false, length = 20)
    private ReviewTypeRole reviewType;
    @JoinColumn(name = "RULE_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private ProjectRuleEntity projectRule;
    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 20)
    private ReviewStatusRole status;
    @JoinColumn(name = "SYSTEM_PROMPT_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private SystemPromptEntity systemPrompt;
    @Column(name = "AI_MODEL", nullable = false, length = 100)
    private String aiModel;
    @Column(name = "GENERAL_RAW_RESPONSE", columnDefinition = "TEXT")
    private String generalRawResponse;
    @Column(name = "RULE_RAW_RESPONSE", columnDefinition = "TEXT")
    private String ruleRawResponse;
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private Instant createdAt;

    private ReviewEntity(ProjectEntity project,
                         ReviewTypeRole reviewType,
                         ProjectRuleEntity projectRule,
                         SystemPromptEntity systemPrompt,
                         String aiModel) {
        this.project = project;
        this.reviewType = reviewType;
        this.projectRule = projectRule;
        this.status = ReviewStatusRole.PENDING;
        this.systemPrompt = systemPrompt;
        this.aiModel = aiModel;
    }

    public static ReviewEntity of(ProjectEntity project,
                                  ReviewTypeRole reviewType,
                                  ProjectRuleEntity projectRule,
                                  SystemPromptEntity systemPrompt,
                                  String aiModel) {
        return new ReviewEntity(project,
                                reviewType,
                                projectRule,
                                systemPrompt,
                                aiModel);
    }

    @PrePersist
    private void onCreated() {
        createdAt = Instant.now();
    }

    public void start() {
        this.status = ReviewStatusRole.PROCESSING;
    }

    public void complete(
            String generalRawResponse,
            String ruleRawResponse
    ) {
        this.generalRawResponse = generalRawResponse;
        this.ruleRawResponse = ruleRawResponse;
        this.status = ReviewStatusRole.COMPLETED;
    }

    public void fail() {
        this.status = ReviewStatusRole.FAILED;
    }
}
