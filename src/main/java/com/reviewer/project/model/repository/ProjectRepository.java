package com.reviewer.project.model.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reviewer.project.model.entity.ProjectEntity;

public interface ProjectRepository extends JpaRepository<ProjectEntity, Long>{

	Optional<ProjectEntity> findByGitRepoOwnerIgnoreCaseAndGitRepoNameIgnoreCase(
			String gitRepoOwner,
			String gitRepoName
	);
}
