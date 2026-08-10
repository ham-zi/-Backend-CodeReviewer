package com.reviewer.review.model.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reviewer.review.model.entity.ReviewItemEntity;

public interface ReviewItemRepository extends JpaRepository<ReviewItemEntity, Long> {

}
