package com.reviewer.review.model.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reviewer.review.model.entity.BranchSourceEntity;

public interface BranchSourceRepository extends JpaRepository<BranchSourceEntity, Long>{

}
