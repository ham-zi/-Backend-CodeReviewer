package com.reviewer.review.metrics.model.dto;

import com.reviewer.review.metrics.model.entity.MetricsEntity;

public record MetricsResponse(
        Long reviewId,
        Integer inputTokenCount,
        Integer outputTokenCount,
        Double responseTimeSeconds
) {

    public static MetricsResponse from(MetricsEntity metrics) {

        return new MetricsResponse(
                metrics.getReviewId(),
                metrics.getInputTokenCount(),
                metrics.getOutputTokenCount(),
                toSeconds(metrics.getResponseTime())
        );
    }

    private static Double toSeconds(Long nanoseconds) {

        if (nanoseconds == null) {
            return null;
        }

        return nanoseconds / 1_000_000_000.0;
    }
}
