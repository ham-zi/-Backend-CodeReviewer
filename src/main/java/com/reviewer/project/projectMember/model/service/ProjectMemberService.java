package com.reviewer.project.projectMember.model.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.reviewer.auth.model.vo.CustomUserDetails;
import com.reviewer.enums.ProjectMemberRole;
import com.reviewer.project.model.dto.ProjectListResponse;
import com.reviewer.project.model.entity.ProjectEntity;
import com.reviewer.project.projectMember.model.Entity.ProjectMemberEntity;
import com.reviewer.project.projectMember.model.repository.ProjectMemberRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProjectMemberService {
	
	private final ProjectMemberRepository projectMemberRepository;
	
	@Transactional
	public void createOwnerMember(ProjectEntity project) {
		projectMemberRepository.save(ProjectMemberEntity.of(project,
															project.getCreatedBy(),
															ProjectMemberRole.OWNER));
	}
	
	public Page<ProjectListResponse> findAllByUserId(CustomUserDetails user, int page) {
		Pageable pageable = PageRequest.of(page-1, 10);
		Page<ProjectEntity> projects = projectMemberRepository.findAllProjectByUserId(user.getUserId(), pageable);
		return projects.map(ProjectListResponse::from);
	}
}
