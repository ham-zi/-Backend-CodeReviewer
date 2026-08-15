package com.reviewer.review.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reviewer.api.model.vo.ApiResponse;
import com.reviewer.auth.model.vo.CustomUserDetails;
import com.reviewer.review.model.dto.BranchReviewRequest;
import com.reviewer.review.model.dto.QuickReviewRequest;
import com.reviewer.review.model.service.ReviewService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
@Slf4j
public class ReviewController {
	
	private final ReviewService reviewService;
	
	@PostMapping("/quick")
	public ResponseEntity<ApiResponse<Void>> quickReview(@AuthenticationPrincipal CustomUserDetails user,
			                                             @Valid @RequestBody QuickReviewRequest reviewRequest){
		reviewService.quickReview(user, reviewRequest);
		return ResponseEntity.ok(ApiResponse.success("코드 리뷰가 완료되었습니다.", null));
	}
	
	@PostMapping("/branch")
	public ResponseEntity<ApiResponse<Long>> branchReview(@AuthenticationPrincipal CustomUserDetails user,
														 @Valid @RequestBody BranchReviewRequest reviewRequest) {
		return ResponseEntity.ok(ApiResponse.success("코드 리뷰가 시작되었습니다.", reviewService.branchReview(user, reviewRequest)));
	}
}
