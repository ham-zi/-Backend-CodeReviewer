package com.reviewer.github.model.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.reviewer.auth.model.vo.CustomUserDetails;
import com.reviewer.github.model.dto.PullRequestResponse;
import com.reviewer.project.model.entity.ProjectEntity;
import com.reviewer.project.validator.ProjectValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class GithubService {

    private final GithubClient githubClient;
    private final ProjectValidator projectValidator;

    public List<PullRequestResponse> getPullRequests(
            Long projectId,
            CustomUserDetails user
    ) {
        projectValidator.checkProjectMember(
                projectId,
                user.getUserId()
        );

        ProjectEntity project =
                projectValidator.existsProject(projectId);

        return githubClient.getPullRequests(
                        project.getGitRepoOwner(),
                        project.getGitRepoName()
                )
                .stream()
                .map(PullRequestResponse::from)
                .toList();
    }
}
