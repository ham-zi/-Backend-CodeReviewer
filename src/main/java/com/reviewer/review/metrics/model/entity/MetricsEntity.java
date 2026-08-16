package com.reviewer.review.metrics.model.entity;

import com.reviewer.review.model.entity.ReviewEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "REVIEW_METRICS")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MetricsEntity {

    @Id
    @Column(name = "review_id")
    private Long reviewId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id")
    private ReviewEntity review;

    /**
     * 입력 프롬프트 토큰 수
     * Ollama: prompt_eval_count
     */
    @Column(name = "prompt_eval_count")
    private Integer promptEvalCount;

    /**
     * 모델이 생성한 출력 토큰 수
     * Ollama: eval_count
     */
    @Column(name = "eval_count")
    private Integer evalCount;

    /**
     * 모델 로딩 시간
     * Ollama: load_duration
     * 단위: nanoseconds
     */
    @Column(name = "load_duration")
    private Long loadDuration;

    /**
     * 입력 프롬프트 처리 시간
     * Ollama: prompt_eval_duration
     * 단위: nanoseconds
     */
    @Column(name = "prompt_eval_duration")
    private Long promptEvalDuration;

    /**
     * 모델 응답 생성 시간
     * Ollama: eval_duration
     * 단위: nanoseconds
     */
    @Column(name = "eval_duration")
    private Long evalDuration;

    /**
     * 전체 요청 처리 시간
     * Ollama: total_duration
     * 단위: nanoseconds
     */
    @Column(name = "total_duration")
    private Long totalDuration;


    public static MetricsEntity create(
            ReviewEntity review,
            Integer promptEvalCount,
            Integer evalCount,
            Long loadDuration,
            Long promptEvalDuration,
            Long evalDuration,
            Long totalDuration
    ) {

        MetricsEntity metrics = new MetricsEntity();

        metrics.review = review;
        metrics.promptEvalCount = promptEvalCount;
        metrics.evalCount = evalCount;
        metrics.loadDuration = loadDuration;
        metrics.promptEvalDuration = promptEvalDuration;
        metrics.evalDuration = evalDuration;
        metrics.totalDuration = totalDuration;

        return metrics;
    }
}