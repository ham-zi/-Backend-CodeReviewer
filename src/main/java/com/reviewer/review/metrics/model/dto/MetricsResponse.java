package com.reviewer.review.metrics.model.dto;

import com.reviewer.review.metrics.model.entity.MetricsEntity;

public record MetricsResponse(

        Long reviewId,

        Integer promptEvalCount,
        Integer evalCount,

        Double loadDurationSeconds,
        Double promptEvalDurationSeconds,
        Double evalDurationSeconds,
        Double totalDurationSeconds,

        Double tokensPerSecond

) {

    public static MetricsResponse from(MetricsEntity metrics) {

        return new MetricsResponse(
                metrics.getReviewId(),
                metrics.getPromptEvalCount(),
                metrics.getEvalCount(),

                toSeconds(metrics.getLoadDuration()),
                toSeconds(metrics.getPromptEvalDuration()),
                toSeconds(metrics.getEvalDuration()),
                toSeconds(metrics.getTotalDuration()),

                calculateTokensPerSecond(
                        metrics.getEvalCount(),
                        metrics.getEvalDuration()
                )
        );
    }

    private static Double toSeconds(Long nanoseconds) {

        if (nanoseconds == null) {
            return null;
        }

        return nanoseconds / 1_000_000_000.0;
    }

    private static Double calculateTokensPerSecond(
            Integer evalCount,
            Long evalDuration
    ) {

        if (evalCount == null ||
            evalDuration == null ||
            evalDuration == 0) {
            return null;
        }

        double seconds = evalDuration / 1_000_000_000.0;

        return evalCount / seconds;
    }
}