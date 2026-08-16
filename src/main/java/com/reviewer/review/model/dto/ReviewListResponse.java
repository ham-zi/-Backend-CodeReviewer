package com.reviewer.review.model.dto;

import java.time.Instant;

import com.reviewer.enums.ReviewStatusRole;
import com.reviewer.enums.ReviewTypeRole;
import com.reviewer.review.model.entity.ReviewEntity;

public record ReviewListResponse(
        Long reviewId,
        ReviewTypeRole reviewType,
        ReviewStatusRole status,
        String aiModel,
        Instant createdAt
) {

    public static ReviewListResponse from(ReviewEntity review) {
        return new ReviewListResponse(
                review.getReviewId(),
                review.getReviewType(),
                review.getStatus(),
                review.getAiModel(),
                review.getCreatedAt()
        );
    }
}