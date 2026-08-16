package com.reviewer.review.model.dto;

import java.time.Instant;
import java.util.List;

import com.reviewer.enums.ReviewStatusRole;
import com.reviewer.enums.ReviewTypeRole;
import com.reviewer.review.metrics.model.dto.MetricsResponse;
import com.reviewer.review.model.entity.ReviewEntity;
import com.reviewer.review.model.entity.ReviewItemEntity;

public record ReviewDetailResponse(
        Long reviewId,
        ReviewTypeRole reviewType,
        ReviewStatusRole status,
        String aiModel,
        Instant createdAt,
        List<ReviewItemResponse> items,
        MetricsResponse metrics
) {

    public static ReviewDetailResponse from(
            ReviewEntity review,
            List<ReviewItemEntity> reviewItems,
            MetricsResponse metrics
    ) {

        List<ReviewItemResponse> items =
                reviewItems.stream()
                        .map(ReviewItemResponse::from)
                        .toList();

        return new ReviewDetailResponse(
                review.getReviewId(),
                review.getReviewType(),
                review.getStatus(),
                review.getAiModel(),
                review.getCreatedAt(),
                items,
                metrics
        );
    }
}