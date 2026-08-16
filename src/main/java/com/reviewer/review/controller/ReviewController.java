package com.reviewer.review.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.reviewer.api.model.vo.ApiResponse;
import com.reviewer.auth.model.vo.CustomUserDetails;
import com.reviewer.enums.ReviewTypeRole;
import com.reviewer.review.model.dto.BranchReviewRequest;
import com.reviewer.review.model.dto.QuickReviewRequest;
import com.reviewer.review.model.dto.ReviewDetailResponse;
import com.reviewer.review.model.dto.ReviewListResponse;
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
	
	@GetMapping
	public ResponseEntity<ApiResponse<Page<ReviewListResponse>>> findAllByProjectId(@AuthenticationPrincipal CustomUserDetails user,
																					@RequestParam(name="projectId") Long projectId,
																					@RequestParam(name="reviewType") ReviewTypeRole type,
																					@RequestParam(name="page", defaultValue="1") int page) {
		return ResponseEntity.ok(ApiResponse.success("코드 리뷰 목록 조회에 성공했습니다.", reviewService.findAllByProjectId(user, projectId, type, page)));
	}
			
	@GetMapping("/{reviewId}")
	public ResponseEntity<ApiResponse<ReviewDetailResponse>> findByReviewId(@AuthenticationPrincipal CustomUserDetails user,
																			@PathVariable(name="reviewId") Long reviewId) {
		return ResponseEntity.ok(ApiResponse.success("코드 리뷰 상세조회에 성공했습니다.", reviewService.findByReviewId(user, reviewId)));
	}
	
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
