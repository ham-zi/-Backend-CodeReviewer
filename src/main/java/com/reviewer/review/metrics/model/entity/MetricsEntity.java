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
    @Column(name = "REVIEW_ID")
    private Long reviewId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REVIEW_ID")
    private ReviewEntity review;

    /**
     * Provider 공통 입력 토큰 수
     * Ollama: prompt_eval_count
     * OpenAI: input_tokens
     */
    @Column(name = "INPUT_TOKEN_COUNT")
    private Integer inputTokenCount;

    /**
     * Provider 공통 출력 토큰 수
     * Ollama: eval_count
     * OpenAI: output_tokens
     */
    @Column(name = "OUTPUT_TOKEN_COUNT")
    private Integer outputTokenCount;

    /**
     * AI API 실제 호출에 걸린 시간
     * System.nanoTime()의 차이값을 사용한다.
     * 단위: nanoseconds
     */
    @Column(name = "RESPONSE_TIME")
    private Long responseTime;

    public static MetricsEntity create(
            ReviewEntity review,
            Integer inputTokenCount,
            Integer outputTokenCount,
            Long responseTime
    ) {

        MetricsEntity metrics = new MetricsEntity();

        metrics.review = review;
        metrics.inputTokenCount = inputTokenCount;
        metrics.outputTokenCount = outputTokenCount;
        metrics.responseTime = responseTime;

        return metrics;
    }
}
