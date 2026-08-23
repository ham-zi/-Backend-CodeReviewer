package com.reviewer.ai.model.dto;

public record AiReviewResponse(
        String model,
        String response,
        Integer inputTokenCount,
        Integer outputTokenCount,
        Long responseTime
) {
}
