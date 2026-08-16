package com.reviewer.review.metrics.model.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reviewer.review.metrics.model.entity.MetricsEntity;


public interface MetricsRepository extends JpaRepository<MetricsEntity, Long> {

}
