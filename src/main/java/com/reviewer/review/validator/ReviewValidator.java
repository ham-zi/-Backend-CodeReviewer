package com.reviewer.review.validator;

import org.springframework.stereotype.Component;

import com.reviewer.exception.common.NotFoundException;
import com.reviewer.review.model.dao.ReviewRepository;
import com.reviewer.review.model.entity.ReviewEntity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReviewValidator {
	
	private final ReviewRepository reviewRepository;
	
	public ReviewEntity existsReview(Long reviewId) {
		return reviewRepository.findById(reviewId).orElseThrow(()->new NotFoundException("존재하지 않는 리뷰입니다."));
	}
}
