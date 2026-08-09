package com.reviewer.review.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reviewer.api.model.vo.ApiResponse;
import com.reviewer.auth.model.vo.CustomUserDetails;
import com.reviewer.review.model.dto.ReviewRequest;
import com.reviewer.review.model.service.ReviewService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
@Slf4j
public class ReviewController {
	
	private final ReviewService reviewService;
	
	@PostMapping("/quick")
	public ResponseEntity<ApiResponse<String>> quickReview(@AuthenticationPrincipal CustomUserDetails user,
														 @RequestBody ReviewRequest reviewRequest) {
		String response = reviewService.quickReview(user, reviewRequest);
		return ResponseEntity.ok(ApiResponse.success("빠른 리뷰 성공했습니다.", response));
	}
}
