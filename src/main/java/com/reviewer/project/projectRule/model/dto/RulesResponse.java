package com.reviewer.project.projectRule.model.dto;

import java.time.Instant;

import com.reviewer.project.projectRule.model.entity.ProjectRuleEntity;

public record RulesResponse(
        Long ruleId,
        Long projectId,
        String title,
        String content,
        String version,
        Instant createdAt,
        Instant updatedAt
) {

    public static RulesResponse from(ProjectRuleEntity rule) {
        return new RulesResponse(
                rule.getRuleId(),
                rule.getProject().getProjectId(),
                rule.getTitle(),
                rule.getContent(),
                rule.getVersion(),
                rule.getCreatedAt(),
                rule.getUpdatedAt()
        );
    }
}