package com.reviewer.review.model.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.reviewer.auth.model.vo.CustomUserDetails;
import com.reviewer.github.model.dto.GithubCompareResponse;
import com.reviewer.github.model.service.GithubClient;
import com.reviewer.project.model.entity.ProjectEntity;
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
	
	public void quickReview(CustomUserDetails user, ReviewRequest reviewRequest) {
		ProjectEntity project = projectValidator.existsProject(reviewRequest.projectId());
		projectValidator.checkProjectMember(reviewRequest.projectId(), user.getUserId());
		GithubCompareResponse res = githubClient.compare(project.getGitRepoOwner(), project.getGitRepoName(), reviewRequest.baseBranch(), reviewRequest.headBranch());
		log.info("{}reviewRequest",reviewRequest);
		log.info("{}1234",res);
	}
	
}
