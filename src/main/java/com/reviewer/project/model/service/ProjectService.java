package com.reviewer.project.model.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.reviewer.auth.model.vo.CustomUserDetails;
import com.reviewer.exception.common.NotFoundException;
import com.reviewer.project.model.dto.ProjectDto;
import com.reviewer.project.model.entity.ProjectEntity;
import com.reviewer.project.model.repository.ProjectRepository;
import com.reviewer.project.projectMember.model.service.ProjectMemberService;
import com.reviewer.user.model.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProjectService {

	private final ProjectRepository projectRepository;
	private final UserRepository userRepository;
	private final ProjectMemberService projectMemberService;
	
	@Transactional
	public void saveProject(CustomUserDetails user, ProjectDto project) {
		ProjectEntity entity = ProjectEntity.of(project.getProjectName(), project.getDescription(), project.getGitRepoOwner(), project.getGitRepoName(), project.getDefaultBranch(),userRepository.findById(user.getUserId()).orElseThrow(() -> new NotFoundException("아이디가 존재하지 않습니다.")));
		ProjectEntity result = projectRepository.save(entity);
		projectMemberService.createProject(result);
	}
}
