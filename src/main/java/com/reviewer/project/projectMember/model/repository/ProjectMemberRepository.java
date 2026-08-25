package com.reviewer.project.projectMember.model.repository;

import java.util.List;
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
    boolean existsByProjectAndUser(ProjectEntity project, UserEntity user);

    Optional<ProjectMemberEntity> findByProjectAndProjectMemberRole(ProjectEntity project, ProjectMemberRole role);

    Optional<ProjectMemberEntity> findByProjectAndProjectMemberId(ProjectEntity project, Long projectMemberId);

    List<ProjectMemberEntity> findAllByProjectOrderByJoinedAtAsc(ProjectEntity project);

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
