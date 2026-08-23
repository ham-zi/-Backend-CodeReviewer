package com.reviewer.review.model.dto;

import java.time.Instant;

import com.reviewer.enums.ReviewResultRole;
import com.reviewer.review.model.entity.RuleReviewItemEntity;

public record RuleReviewItemResponse(
        Long ruleReviewItemId,
        ReviewResultRole result,
        String title,
        String location,
        String evidence,
        String description,
        String suggestion,
        Instant createdAt
) {

    public static RuleReviewItemResponse from(RuleReviewItemEntity item) {
        return new RuleReviewItemResponse(
                item.getRuleReviewItemId(),
                item.getResult(),
                item.getTitle(),
                item.getLocation(),
                item.getEvidence(),
                item.getDescription(),
                item.getSuggestion(),
                item.getCreatedAt()
        );
    }
}
