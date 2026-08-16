package com.reviewer.review.model.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.reviewer.enums.ReviewTypeRole;
import com.reviewer.review.model.entity.ReviewEntity;

public interface ReviewRepository extends JpaRepository<ReviewEntity, Long>{
	Page<ReviewEntity> findAllByProject_ProjectIdAndReviewType(
	        Long projectId,
	        ReviewTypeRole reviewType,
	        Pageable pageable
	);
}
