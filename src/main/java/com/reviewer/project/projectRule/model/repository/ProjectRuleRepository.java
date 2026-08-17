package com.reviewer.project.projectRule.model.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.reviewer.project.model.entity.ProjectEntity;
import com.reviewer.project.projectRule.model.entity.ProjectRuleEntity;

public interface ProjectRuleRepository extends JpaRepository<ProjectRuleEntity, Long>{

	Page<ProjectRuleEntity> findAllByProject(ProjectEntity project, Pageable pageable);

}
