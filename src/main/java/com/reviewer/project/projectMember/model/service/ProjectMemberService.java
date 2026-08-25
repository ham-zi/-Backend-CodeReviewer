package com.reviewer.project.projectMember.model.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.reviewer.auth.model.vo.CustomUserDetails;
import com.reviewer.enums.ProjectMemberRole;
import com.reviewer.exception.common.AccessDeniedException;
import com.reviewer.exception.common.DuplicateException;
import com.reviewer.exception.common.NotFoundException;
import com.reviewer.project.model.dto.ProjectListResponse;
import com.reviewer.project.model.entity.ProjectEntity;
import com.reviewer.project.projectMember.model.Entity.ProjectMemberEntity;
import com.reviewer.project.projectMember.model.dto.ProjectMemberCreateRequest;
import com.reviewer.project.projectMember.model.dto.ProjectMemberResponse;
import com.reviewer.project.projectMember.model.repository.ProjectMemberRepository;
import com.reviewer.project.validator.ProjectValidator;
import com.reviewer.user.model.entity.UserEntity;
import com.reviewer.user.model.repository.UserRepository;


@Service
@Transactional(readOnly = true)
public class ProjectMemberService {

    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final ProjectValidator projectValidator;

    public ProjectMemberService(
            ProjectMemberRepository projectMemberRepository,
            UserRepository userRepository,
            ProjectValidator projectValidator) {
        this.projectMemberRepository = projectMemberRepository;
        this.userRepository = userRepository;
        this.projectValidator = projectValidator;
    }

    @Transactional
    public void createOwnerMember(ProjectEntity project) {
        projectMemberRepository.save(ProjectMemberEntity.of(
                project,
                project.getCreatedBy(),
                ProjectMemberRole.OWNER
        ));
    }

    public Page<ProjectListResponse> findAllByUserId(CustomUserDetails user, int page) {
        Pageable pageable = PageRequest.of(page - 1, 10, Sort.by(Sort.Direction.DESC, "project.createdAt"));
        Page<ProjectEntity> projects = projectMemberRepository.findAllProjectByUserId(user.getUserId(), pageable);
        return projects.map(ProjectListResponse::from);
    }

    public List<ProjectMemberResponse> findAllMembers(Long projectId, CustomUserDetails user) {
        ProjectEntity project = projectValidator.checkProjectMember(projectId, user.getUserId());

        return projectMemberRepository.findAllByProjectOrderByJoinedAtAsc(project)
                .stream()
                .map(ProjectMemberResponse::from)
                .toList();
    }

    @Transactional
    public ProjectMemberResponse addMember(
            Long projectId,
            ProjectMemberCreateRequest request,
            CustomUserDetails requester
    ) {
        ProjectEntity project = projectValidator.existsProject(projectId);
        projectValidator.checkProjectOwner(projectId, requester.getUserId());

        String loginId = request.loginId().trim();
        UserEntity targetUser = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 사용자입니다."));

        if (!targetUser.isActive()) {
            throw new AccessDeniedException("비활성 사용자는 프로젝트에 추가할 수 없습니다.");
        }

        if (projectMemberRepository.existsByProjectAndUser(project, targetUser)) {
            throw new DuplicateException("이미 프로젝트에 참여 중인 사용자입니다.");
        }

        ProjectMemberEntity saved = projectMemberRepository.save(
                ProjectMemberEntity.of(project, targetUser, ProjectMemberRole.MEMBER)
        );

        return ProjectMemberResponse.from(saved);
    }

    @Transactional
    public void removeMember(Long projectId, Long projectMemberId, CustomUserDetails requester) {
        ProjectEntity project = projectValidator.existsProject(projectId);
        projectValidator.checkProjectOwner(projectId, requester.getUserId());

        ProjectMemberEntity member = projectMemberRepository
                .findByProjectAndProjectMemberId(project, projectMemberId)
                .orElseThrow(() -> new NotFoundException("프로젝트에 존재하지 않는 팀원입니다."));

        if (member.getProjectMemberRole() == ProjectMemberRole.OWNER) {
            throw new AccessDeniedException("프로젝트 OWNER는 삭제할 수 없습니다.");
        }

        projectMemberRepository.delete(member);
    }
}
