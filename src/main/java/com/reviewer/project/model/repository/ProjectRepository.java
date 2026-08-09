package com.reviewer.project.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reviewer.project.model.entity.ProjectEntity;

public interface ProjectRepository extends JpaRepository<ProjectEntity, Long>{

}
