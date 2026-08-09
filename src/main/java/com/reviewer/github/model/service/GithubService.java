package com.reviewer.github.model.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.reviewer.auth.model.vo.CustomUserDetails;
import com.reviewer.exception.common.NotFoundException;
import com.reviewer.github.model.dto.BranchResponse;
import com.reviewer.project.model.entity.ProjectEntity;
import com.reviewer.project.model.repository.ProjectRepository;
import com.reviewer.project.validator.ProjectValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class GithubService {
	private final GithubClient githubClient;
	private final ProjectRepository projectRepository;
	private final ProjectValidator projectValidator; 
	
	public List<BranchResponse> getBranches(Long projectId, CustomUserDetails user) {
		projectValidator.checkProjectMember(projectId, user.getUserId());
		ProjectEntity project = projectRepository.findById(projectId).orElseThrow(() -> new NotFoundException("존재하지 않는 프로젝트입니다."));
		return githubClient.getBranches(project.getGitRepoOwner(), project.getGitRepoName()).stream().map(branch -> new BranchResponse(branch.name())).toList();
	}
	

	
}
