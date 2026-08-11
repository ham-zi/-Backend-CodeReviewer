package com.reviewer.review.model.service;

import org.springframework.stereotype.Service;

import com.reviewer.enums.ReviewResultRole;
import com.reviewer.exception.common.NotFoundException;
import com.reviewer.review.model.dao.ReviewItemRepository;
import com.reviewer.review.model.dao.ReviewRepository;
import com.reviewer.review.model.dto.ReviewProcessData;
import com.reviewer.review.model.entity.ReviewEntity;
import com.reviewer.review.model.entity.ReviewItemEntity;

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

    @Transactional
    public ReviewProcessData start(Long reviewId) {

        ReviewEntity review = reviewRepository.findById(reviewId)
                .orElseThrow(() ->
                        new NotFoundException("존재하지 않는 리뷰입니다."));

        review.start();

        return new ReviewProcessData(
                review.getProjectRule().getContent()
        );
    }

    @Transactional
    public void complete(
            Long reviewId,
            String rawResponse,
            JsonNode json) {
    	log.info("RAW RESPONSE = {}", rawResponse);
    	log.info("PARSED JSON = {}", json);

        ReviewEntity review = reviewRepository.findById(reviewId)
                .orElseThrow(() ->
                        new NotFoundException("존재하지 않는 리뷰입니다."));

        JsonNode reviews = json.get("reviews");

        for (JsonNode r : reviews) {

            ReviewItemEntity item =
                    ReviewItemEntity.of(
                            review,
                            ReviewResultRole.VIOLATION,
                            r.get("title").asText(),
                            r.get("location").asText(),
                            r.get("evidence").asText(),
                            r.get("description").asText(),
                            r.get("suggestion").asText()
                    );

            reviewItemRepository.save(item);
        }
        review.complete(rawResponse);
    }

    @Transactional
    public void fail(Long reviewId) {

        ReviewEntity review = reviewRepository.findById(reviewId)
                .orElseThrow(() ->
                        new NotFoundException("존재하지 않는 리뷰입니다."));

        review.fail();
    }
}