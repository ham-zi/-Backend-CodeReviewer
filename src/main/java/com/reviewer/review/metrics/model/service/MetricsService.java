package com.reviewer.review.metrics.model.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.reviewer.exception.common.NotFoundException;
import com.reviewer.ollama.model.dto.OllamaResponse;
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
@Transactional(readOnly=true)
public class MetricsService {

    private final MetricsRepository metricsRepository;
    private final ReviewRepository reviewRepository;

    @Transactional
    public void saveMetrics(Long reviewId, OllamaResponse ollamaResponse) {
        ReviewEntity review = reviewRepository.findById(reviewId)
                .orElseThrow(() ->
                        new IllegalArgumentException("존재하지 않는 리뷰입니다. reviewId=" + reviewId)
                );
        MetricsEntity metrics = MetricsEntity.create(
                review,
                ollamaResponse.promptEvalCount(),
                ollamaResponse.evalCount(),
                ollamaResponse.loadDuration(),
                ollamaResponse.promptEvalDuration(),
                ollamaResponse.evalDuration(),
                ollamaResponse.totalDuration()
        );
        metricsRepository.save(metrics);
    }
    
    public MetricsResponse findByReviewId(Long reviewId) {
    	MetricsEntity metrics = metricsRepository.findById(reviewId).orElse(null);
    	if(metrics == null) return null;
    	return MetricsResponse.from(metrics);
    }
    
}