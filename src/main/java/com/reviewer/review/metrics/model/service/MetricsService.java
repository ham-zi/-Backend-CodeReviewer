package com.reviewer.review.metrics.model.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.reviewer.review.metrics.model.dao.MetricsRepository;
import com.reviewer.review.metrics.model.entity.MetricsEntity;
import com.reviewer.review.model.dao.ReviewRepository;
import com.reviewer.review.model.entity.ReviewEntity;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.JsonNode;

@Service
@RequiredArgsConstructor
public class MetricsService {

    private final MetricsRepository metricsRepository;
    private final ReviewRepository reviewRepository;

    @Transactional
    public void saveMetrics(Long reviewId, JsonNode ollamaResponse) {

        ReviewEntity review = reviewRepository.findById(reviewId)
                .orElseThrow(() ->
                        new IllegalArgumentException("존재하지 않는 리뷰입니다. reviewId=" + reviewId)
                );

        Integer promptEvalCount =
                ollamaResponse.path("prompt_eval_count").asInt();

        Integer evalCount =
                ollamaResponse.path("eval_count").asInt();

        Long promptEvalDuration =
                ollamaResponse.path("prompt_eval_duration").asLong();

        Long evalDuration =
                ollamaResponse.path("eval_duration").asLong();

        Long totalDuration =
                ollamaResponse.path("total_duration").asLong();

        MetricsEntity metrics = MetricsEntity.create(
                review,
                promptEvalCount,
                evalCount,
                promptEvalDuration,
                evalDuration,
                totalDuration
        );

        metricsRepository.save(metrics);
    }
}