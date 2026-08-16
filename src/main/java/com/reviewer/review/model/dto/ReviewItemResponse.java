package com.reviewer.review.model.dto;

import java.time.Instant;

import com.reviewer.enums.ReviewResultRole;
import com.reviewer.review.model.entity.ReviewItemEntity;

public record ReviewItemResponse(

        Long reviewItemId,
        ReviewResultRole result,
        String title,
        String location,
        String evidence,
        String description,
        String suggestion,
        Instant createdAt

) {

    public static ReviewItemResponse from(ReviewItemEntity item) {

        return new ReviewItemResponse(
                item.getReviewItemId(),
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