package com.reviewer.project.projectMember.model.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.reviewer.enums.ProjectMemberRole;
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
}
