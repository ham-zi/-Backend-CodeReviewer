package com.reviewer.review.model.service;

import org.springframework.stereotype.Service;

import com.reviewer.ai.model.dto.AiReviewResponse;
import com.reviewer.enums.ReviewResultRole;
import com.reviewer.exception.common.NotFoundException;
import com.reviewer.review.metrics.model.service.MetricsService;
import com.reviewer.review.model.dao.PrSourceRepository;
import com.reviewer.review.model.dao.QuickSourceRepository;
import com.reviewer.review.model.dao.ReviewItemRepository;
import com.reviewer.review.model.dao.ReviewRepository;
import com.reviewer.review.model.dao.RuleReviewItemRepository;
import com.reviewer.review.model.dto.PrReviewProcessData;
import com.reviewer.review.model.dto.ReviewProcessData;
import com.reviewer.review.model.entity.PrSourceEntity;
import com.reviewer.review.model.entity.QuickSourceEntity;
import com.reviewer.review.model.entity.ReviewEntity;
import com.reviewer.review.model.entity.ReviewItemEntity;
import com.reviewer.review.model.entity.RuleReviewItemEntity;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.JsonNode;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewTransactionService {

    private final ReviewRepository reviewRepository;
    private final ReviewItemRepository reviewItemRepository;
    private final RuleReviewItemRepository ruleReviewItemRepository;
    private final QuickSourceRepository quickSourceRepository;
    private final PrSourceRepository prSourceRepository;
    private final MetricsService metricsService;

    @Transactional
    public ReviewProcessData startQuick(Long reviewId) {

        ReviewEntity review = getReview(reviewId);

        QuickSourceEntity source = quickSourceRepository.findById(reviewId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "QUICK 리뷰 코드가 존재하지 않습니다."
                        )
                );

        review.start();

        return new ReviewProcessData(
                source.getInputCode(),
                review.getProjectRule().getContent(),
                review.getSystemPrompt().getGeneralPrompt(),
                review.getSystemPrompt().getRulePrompt()
        );
    }

    @Transactional
    public PrReviewProcessData startPr(Long reviewId) {

        ReviewEntity review = getReview(reviewId);

        PrSourceEntity source = prSourceRepository.findById(reviewId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "PR 리뷰 정보가 존재하지 않습니다."
                        )
                );

        review.start();

        return new PrReviewProcessData(
                review.getProjectRule().getContent(),
                review.getProject().getGitRepoOwner(),
                review.getProject().getGitRepoName(),
                source.getPullNumber(),
                review.getSystemPrompt().getGeneralPrompt(),
                review.getSystemPrompt().getRulePrompt()
        );
    }

    /**
     * AI 호출은 트랜잭션 밖에서 모두 끝낸 뒤 이 메서드에서 한 번에 저장한다.
     * 일반 리뷰/규칙 리뷰/metrics/완료 상태를 같은 트랜잭션으로 묶어
     * 중간 저장만 남는 상황을 줄인다.
     */
    @Transactional
    public void complete(
            Long reviewId,
            AiReviewResponse generalResponse,
            AiReviewResponse ruleResponse,
            JsonNode generalJson,
            JsonNode ruleJson
    ) {

        ReviewEntity review = getReview(reviewId);

        saveGeneralReview(review, generalJson);
        saveRuleReview(review, ruleJson);

        metricsService.saveMetrics(
                reviewId,
                generalResponse,
                ruleResponse
        );

        review.complete(
                generalResponse.response(),
                ruleResponse.response()
        );
    }

    private void saveGeneralReview(
            ReviewEntity review,
            JsonNode json
    ) {

        JsonNode reviews = getReviews(json, "일반 리뷰");

        for (JsonNode r : reviews) {

            ReviewItemEntity item = ReviewItemEntity.of(
                    review,
                    ReviewResultRole.valueOf(r.get("status").asText()),
                    r.get("title").asText(),
                    r.get("location").asText(),
                    r.get("evidence").asText(),
                    r.get("description").asText(),
                    r.get("suggestion").asText()
            );

            reviewItemRepository.save(item);
        }
    }

    private void saveRuleReview(
            ReviewEntity review,
            JsonNode json
    ) {

        JsonNode reviews = getReviews(json, "규칙 리뷰");

        for (JsonNode r : reviews) {

            RuleReviewItemEntity item = RuleReviewItemEntity.of(
                    review,
                    ReviewResultRole.valueOf(r.get("status").asText()),
                    r.get("title").asText(),
                    r.get("location").asText(),
                    r.get("evidence").asText(),
                    r.get("description").asText(),
                    r.get("suggestion").asText()
            );

            ruleReviewItemRepository.save(item);
        }
    }

    private JsonNode getReviews(
            JsonNode json,
            String reviewName
    ) {

        JsonNode reviews = json.get("reviews");

        if (reviews == null || !reviews.isArray()) {
            throw new IllegalStateException(
                    reviewName + " 응답에 reviews 배열이 존재하지 않습니다."
            );
        }

        return reviews;
    }

    @Transactional
    public void fail(Long reviewId) {

        ReviewEntity review = getReview(reviewId);
        review.fail();
    }

    private ReviewEntity getReview(Long reviewId) {

        return reviewRepository.findById(reviewId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "존재하지 않는 리뷰입니다."
                        )
                );
    }
}
