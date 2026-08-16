package com.reviewer.project.projectMember.model.repository;


import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.reviewer.enums.ProjectMemberRole;
import com.reviewer.project.model.entity.ProjectEntity;
import com.reviewer.project.projectMember.model.Entity.ProjectMemberEntity;
import com.reviewer.user.model.entity.UserEntity;

public interface ProjectMemberRepository extends JpaRepository<ProjectMemberEntity, Long> {
	boolean existsByProjectAndUser(ProjectEntity project, UserEntity userId);
	Optional<ProjectMemberEntity> findByProjectAndProjectMemberRole(ProjectEntity project, ProjectMemberRole role);
	@Query("""
		    SELECT pm.project
		    FROM ProjectMemberEntity pm
		    WHERE pm.user.id = :userId
		   """)
		Page<ProjectEntity> findAllProjectByUserId(
		        @Param("userId") Long userId,
		        Pageable pageable
		);
}
