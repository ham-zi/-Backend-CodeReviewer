package com.reviewer.review.model.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.reviewer.auth.model.vo.CustomUserDetails;
import com.reviewer.enums.ReviewResultRole;
import com.reviewer.enums.ReviewTypeRole;
import com.reviewer.exception.common.NotFoundException;
import com.reviewer.ollama.client.OllamaClient;
import com.reviewer.ollama.model.dto.OllamaResponse;
import com.reviewer.project.model.entity.ProjectEntity;
import com.reviewer.project.validator.ProjectValidator;
import com.reviewer.review.metrics.model.service.MetricsService;
import com.reviewer.review.model.dao.BranchSourceRepository;
import com.reviewer.review.model.dao.QuickSourceRepository;
import com.reviewer.review.model.dao.ReviewItemRepository;
import com.reviewer.review.model.dao.ReviewRepository;
import com.reviewer.review.model.dto.BranchReviewRequest;
import com.reviewer.review.model.dto.QuickReviewRequest;
import com.reviewer.review.model.dto.ReviewDetailResponse;
import com.reviewer.review.model.dto.ReviewListResponse;
import com.reviewer.review.model.entity.BranchSourceEntity;
import com.reviewer.review.model.entity.QuickSourceEntity;
import com.reviewer.review.model.entity.ReviewEntity;
import com.reviewer.review.model.entity.ReviewItemEntity;
import com.reviewer.review.validator.ReviewValidator;
import com.reviewer.system.model.dao.SystemSettingRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly=true)
public class ReviewService {
	
	private final ProjectValidator projectValidator;
	private final OllamaClient ollamaClient;
	private final ReviewAsyncService reviewAsyncService;
	private final ReviewRepository reviewRepository;
	private final QuickSourceRepository quickSourceRepository;
	private final BranchSourceRepository branchSourceRepository;
	private final ReviewItemRepository reviewItemRepository;
    private final JsonMapper jsonMapper;
    private final SystemSettingRepository systemSettingRepository;
    private final MetricsService metricsService;
    private final ReviewValidator reviewValidator;
    @Value("${app.ollama.model}")
    private String model;
	
	@Transactional
	public void quickReview(CustomUserDetails user, QuickReviewRequest reviewRequest) {
		ProjectEntity project = projectValidator.existsProject(reviewRequest.projectId());
		projectValidator.checkProjectMember(reviewRequest.projectId(), user.getUserId());
		ReviewEntity review = reviewRepository.save(ReviewEntity.of(project, 
													ReviewTypeRole.QUICK,
													project.getProjectRule(),
													systemSettingRepository.findById(ReviewTypeRole.QUICK).orElseThrow(()-> new NotFoundException("존재하지 않는 리뷰타입입니다.")).getSystemPrompt(),
													model));
		quickSourceRepository.save(QuickSourceEntity.of(review, reviewRequest.code()));
		OllamaResponse ollamaResponse = quickReview(review, reviewRequest.code());
		review.complete(ollamaResponse.response());
		
        JsonNode json =
                jsonMapper.readTree(ollamaResponse.response());
        
        JsonNode reviews = json.get("reviews");
        if (reviews == null || !reviews.isArray()) {
            throw new IllegalStateException(
                    "Ollama 응답에 reviews 배열이 존재하지 않습니다."
            );
        }
        
        
        metricsService.saveMetrics(review.getReviewId(), ollamaResponse);
        for (JsonNode r : reviews) {

            ReviewItemEntity item =
                    ReviewItemEntity.of(
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
	
	private OllamaResponse quickReview(ReviewEntity review, String code) {
		StringBuilder sb = new StringBuilder();
		sb.append("##리뷰 대상 코드##");
		sb.append(code);
		sb.append("##팀 규칙##");
		sb.append(review.getProjectRule().getContent());
		return ollamaClient.quickReview(sb.toString());
	}
	
	@Transactional
	public Long branchReview(CustomUserDetails user, BranchReviewRequest reviewRequest) {
		ProjectEntity project = projectValidator.existsProject(reviewRequest.projectId());
		projectValidator.checkProjectMember(reviewRequest.projectId(), user.getUserId());
		ReviewEntity review = reviewRepository.save(ReviewEntity.of(project,
				                               ReviewTypeRole.BRANCH,
				                               project.getProjectRule(),
				                               systemSettingRepository.findById(ReviewTypeRole.BRANCH).orElseThrow(()->new NotFoundException("존재하지 않는 리뷰타입입니다.")).getSystemPrompt(),
				                               model
				                               ));
		branchSourceRepository.save(BranchSourceEntity.of(review, reviewRequest.baseBranch(), reviewRequest.headBranch()));
		TransactionSynchronizationManager.registerSynchronization( new TransactionSynchronization() {
			@Override
			public void afterCommit() {
				reviewAsyncService.process(review.getReviewId(), reviewRequest);
				}
			}
		);
			return review.getReviewId();
	}
	
	public Page<ReviewListResponse> findAllByProjectId(CustomUserDetails user, Long projectId, ReviewTypeRole reviewType, int page) {
		projectValidator.checkProjectMember(projectId, user.getUserId());
		Pageable pageable = PageRequest.of(page-1, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
		Page<ReviewEntity> reviews = reviewRepository.findAllByProject_ProjectIdAndReviewType(projectId, reviewType, pageable);
		return reviews.map(ReviewListResponse::from);
	}
	
	public ReviewDetailResponse findByReviewId(CustomUserDetails user, Long reviewId) {
		ReviewEntity review = reviewValidator.existsReview(reviewId);
		projectValidator.checkProjectMember(review.getProject().getProjectId(), user.getUserId());
		return ReviewDetailResponse.from(review, 
										 reviewItemRepository.findAllByReview(review),
										 metricsService.findByReviewId(reviewId));
	}
	
	
	
}
