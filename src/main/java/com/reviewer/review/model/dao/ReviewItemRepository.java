package com.reviewer.review.model.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reviewer.review.model.entity.ReviewEntity;
import com.reviewer.review.model.entity.ReviewItemEntity;

public interface ReviewItemRepository extends JpaRepository<ReviewItemEntity, Long> {
	List<ReviewItemEntity> findAllByReview(ReviewEntity review);
}
