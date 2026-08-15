package com.reviewer.project.projectRule.model.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reviewer.project.model.entity.ProjectEntity;
import com.reviewer.project.projectRule.model.entity.ProjectRuleEntity;

public interface ProjectRuleRepository extends JpaRepository<ProjectRuleEntity, Long>{

	List<ProjectRuleEntity> findAllByProject(ProjectEntity project);

}
