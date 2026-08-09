package com.reviewer.project.projectMember.model.repository;


import org.springframework.data.jpa.repository.JpaRepository;


import com.reviewer.project.projectMember.model.Entity.ProjectMemberEntity;

public interface ProjectMemberRepository extends JpaRepository<ProjectMemberEntity, Long> {
	boolean existsByProjectAndUser(Long projectId, Long userId);
}
