package com.reviewer.review.model.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.reviewer.ai.client.AiReviewClient;
import com.reviewer.auth.model.vo.CustomUserDetails;
import com.reviewer.enums.ReviewTypeRole;
import com.reviewer.exception.common.NotFoundException;
import com.reviewer.project.model.entity.ProjectEntity;
import com.reviewer.project.validator.ProjectValidator;
import com.reviewer.review.metrics.model.service.MetricsService;
import com.reviewer.review.model.dao.PrSourceRepository;
import com.reviewer.review.model.dao.QuickSourceRepository;
import com.reviewer.review.model.dao.ReviewItemRepository;
import com.reviewer.review.model.dao.ReviewRepository;
import com.reviewer.review.model.dao.RuleReviewItemRepository;
import com.reviewer.review.model.dto.PrReviewRequest;
import com.reviewer.review.model.dto.QuickReviewRequest;
import com.reviewer.review.model.dto.ReviewDetailResponse;
import com.reviewer.review.model.dto.ReviewListResponse;
import com.reviewer.review.model.entity.PrSourceEntity;
import com.reviewer.review.model.entity.QuickSourceEntity;
import com.reviewer.review.model.entity.ReviewEntity;
import com.reviewer.review.validator.ReviewValidator;
import com.reviewer.system.model.Entity.SystemPromptEntity;
import com.reviewer.system.model.dao.SystemSettingRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ReviewService {

    private final ProjectValidator projectValidator;
    private final ReviewAsyncService reviewAsyncService;
    private final ReviewRepository reviewRepository;
    private final QuickSourceRepository quickSourceRepository;
    private final PrSourceRepository prSourceRepository;
    private final ReviewItemRepository reviewItemRepository;
    private final RuleReviewItemRepository ruleReviewItemRepository;
    private final SystemSettingRepository systemSettingRepository;
    private final MetricsService metricsService;
    private final ReviewValidator reviewValidator;
    private final AiReviewClient aiReviewClient;

    @Transactional
    public Long quickReview(
            CustomUserDetails user,
            QuickReviewRequest request
    ) {

        ProjectEntity project =
                projectValidator.existsProject(request.projectId());

        projectValidator.checkProjectMember(
                request.projectId(),
                user.getUserId()
        );

        SystemPromptEntity systemPrompt =
                getSystemPrompt(ReviewTypeRole.QUICK);

        ReviewEntity review = reviewRepository.save(
                ReviewEntity.of(
                        project,
                        ReviewTypeRole.QUICK,
                        project.getProjectRule(),
                        systemPrompt,
                        aiReviewClient.getModel()
                )
        );

        quickSourceRepository.save(
                QuickSourceEntity.of(
                        review,
                        request.code()
                )
        );

        runAfterCommit(() ->
                reviewAsyncService.processQuick(
                        review.getReviewId()
                )
        );

        return review.getReviewId();
    }

    @Transactional
    public Long prReview(
            CustomUserDetails user,
            PrReviewRequest request
    ) {

        ProjectEntity project =
                projectValidator.existsProject(request.projectId());

        projectValidator.checkProjectMember(
                request.projectId(),
                user.getUserId()
        );

        SystemPromptEntity systemPrompt =
                getSystemPrompt(ReviewTypeRole.PR);

        ReviewEntity review = reviewRepository.save(
                ReviewEntity.of(
                        project,
                        ReviewTypeRole.PR,
                        project.getProjectRule(),
                        systemPrompt,
                        aiReviewClient.getModel()
                )
        );

        prSourceRepository.save(
                PrSourceEntity.of(
                        review,
                        request.pullNumber()
                )
        );

        runAfterCommit(() ->
                reviewAsyncService.processPr(
                        review.getReviewId()
                )
        );

        return review.getReviewId();
    }

    public Page<ReviewListResponse> findAllByProjectId(
            CustomUserDetails user,
            Long projectId,
            ReviewTypeRole reviewType,
            int page
    ) {
        projectValidator.checkProjectMember(
                projectId,
                user.getUserId()
        );

        Pageable pageable = PageRequest.of(
                page - 1,
                10,
                Sort.by(
                        Sort.Direction.DESC,
                        "createdAt"
                )
        );

        Page<ReviewEntity> reviews =
                reviewRepository
                        .findAllByProject_ProjectIdAndReviewType(
                                projectId,
                                reviewType,
                                pageable
                        );

        return reviews.map(ReviewListResponse::from);
    }

    public ReviewDetailResponse findByReviewId(
            CustomUserDetails user,
            Long reviewId
    ) {
        ReviewEntity review =
                reviewValidator.existsReview(reviewId);

        projectValidator.checkProjectMember(
                review.getProject().getProjectId(),
                user.getUserId()
        );

        return ReviewDetailResponse.from(
                review,
                reviewItemRepository.findAllByReview(review),
                ruleReviewItemRepository.findAllByReview(review),
                metricsService.findByReviewId(reviewId)
        );
    }

    private SystemPromptEntity getSystemPrompt(
            ReviewTypeRole reviewType
    ) {
        return systemSettingRepository.findById(reviewType)
                .orElseThrow(() ->
                        new NotFoundException(
                                "존재하지 않는 리뷰타입입니다."
                        )
                )
                .getSystemPrompt();
    }

    private void runAfterCommit(Runnable task) {
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        task.run();
                    }
                }
        );
    }
}
