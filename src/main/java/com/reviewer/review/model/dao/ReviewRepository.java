package com.reviewer.review.model.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reviewer.review.model.entity.ReviewEntity;

public interface ReviewRepository extends JpaRepository<ReviewEntity, Long>{

}
