package com.reviewer.review.model.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.reviewer.auth.model.vo.CustomUserDetails;
import com.reviewer.github.model.dto.GithubCompareResponse;
import com.reviewer.github.model.dto.GithubFileResponse;
import com.reviewer.github.model.service.GithubClient;
import com.reviewer.ollama.client.OllamaClient;
import com.reviewer.project.model.entity.ProjectEntity;
import com.reviewer.project.projectRule.model.repository.ProjectRuleRepository;
import com.reviewer.project.validator.ProjectValidator;
import com.reviewer.review.model.dto.ReviewRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly=true)
public class ReviewService {
	
	private final ProjectValidator projectValidator;
	private final GithubClient githubClient;
	private final OllamaClient ollamaClient;
	private final ProjectRuleRepository projectRuleRepository;
	
	public String quickReview(CustomUserDetails user, ReviewRequest reviewRequest) {
		ProjectEntity project = projectValidator.existsProject(reviewRequest.projectId());
		projectValidator.checkProjectMember(reviewRequest.projectId(), user.getUserId());
		GithubCompareResponse response = githubClient.compare(project.getGitRepoOwner(), project.getGitRepoName(), reviewRequest.baseBranch(), reviewRequest.headBranch());
		return testOllama(project, response);
	}
	
	public String testOllama(ProjectEntity project, GithubCompareResponse res) {
		StringBuilder sb = new StringBuilder();
		for (GithubFileResponse file : res.files()) {
		    sb.append("파일: ").append(file.filename()).append("\n");
		    sb.append("변경사항:\n");
		    sb.append(file.patch()).append("\n\n");
		}
		log.info("PROMPT LENGTH = {}", sb.length());
		log.info("FILE COUNT = {}", res.files().size());
		sb.append(project.getProjectRule().getContent());
	    return ollamaClient.generate(
	        		sb.toString()
	        		);

	}
	
}
