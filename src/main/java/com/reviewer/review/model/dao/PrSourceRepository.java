package com.reviewer.review.model.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reviewer.review.model.entity.PrSourceEntity;

public interface PrSourceRepository extends JpaRepository<PrSourceEntity, Long> {
}
