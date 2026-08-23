package com.reviewer.review.metrics.model.service;

import java.util.Arrays;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.reviewer.ai.model.dto.AiReviewResponse;
import com.reviewer.review.metrics.model.dao.MetricsRepository;
import com.reviewer.review.metrics.model.dto.MetricsResponse;
import com.reviewer.review.metrics.model.entity.MetricsEntity;
import com.reviewer.review.model.dao.ReviewRepository;
import com.reviewer.review.model.entity.ReviewEntity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class MetricsService {

    private final MetricsRepository metricsRepository;
    private final ReviewRepository reviewRepository;

    @Transactional
    public void saveMetrics(
            Long reviewId,
            AiReviewResponse... responses
    ) {
        ReviewEntity review = reviewRepository.findById(reviewId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "존재하지 않는 리뷰입니다. reviewId=" + reviewId
                        )
                );

        Integer inputTokenCount = sumInteger(
                Arrays.stream(responses)
                        .map(AiReviewResponse::inputTokenCount)
                        .toArray(Integer[]::new)
        );

        Integer outputTokenCount = sumInteger(
                Arrays.stream(responses)
                        .map(AiReviewResponse::outputTokenCount)
                        .toArray(Integer[]::new)
        );

        Long responseTime = sumLong(
                Arrays.stream(responses)
                        .map(AiReviewResponse::responseTime)
                        .toArray(Long[]::new)
        );

        MetricsEntity metrics = MetricsEntity.create(
                review,
                inputTokenCount,
                outputTokenCount,
                responseTime
        );

        metricsRepository.save(metrics);
    }

    public MetricsResponse findByReviewId(Long reviewId) {
        MetricsEntity metrics = metricsRepository.findById(reviewId)
                .orElse(null);

        if (metrics == null) {
            return null;
        }

        return MetricsResponse.from(metrics);
    }

    private Integer sumInteger(Integer... values) {
        int sum = 0;
        boolean exists = false;

        for (Integer value : values) {
            if (value != null) {
                sum += value;
                exists = true;
            }
        }

        return exists ? sum : null;
    }

    private Long sumLong(Long... values) {
        long sum = 0L;
        boolean exists = false;

        for (Long value : values) {
            if (value != null) {
                sum += value;
                exists = true;
            }
        }

        return exists ? sum : null;
    }
}
