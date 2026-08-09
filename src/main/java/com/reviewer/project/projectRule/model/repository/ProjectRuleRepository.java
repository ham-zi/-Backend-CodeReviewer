package com.reviewer.project.projectRule.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reviewer.project.projectRule.model.entity.ProjectRuleEntity;

public interface ProjectRuleRepository extends JpaRepository<ProjectRuleEntity, Long>{

}
