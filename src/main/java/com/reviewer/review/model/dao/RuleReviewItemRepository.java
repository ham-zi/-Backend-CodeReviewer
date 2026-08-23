package com.reviewer.review.model.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reviewer.review.model.entity.ReviewEntity;
import com.reviewer.review.model.entity.RuleReviewItemEntity;

public interface RuleReviewItemRepository extends JpaRepository<RuleReviewItemEntity, Long> {
    List<RuleReviewItemEntity> findAllByReview(ReviewEntity review);
}
