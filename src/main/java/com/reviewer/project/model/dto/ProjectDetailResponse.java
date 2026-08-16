package com.reviewer.project.model.dto;

import java.time.Instant;

import com.reviewer.project.model.entity.ProjectEntity;

public record ProjectDetailResponse(
        Long projectId,
        String projectName,
        String description,
        String repositoryUrl,
        String gitRepoOwner,
        String gitRepoName,
        String defaultBranch,
        Instant createdAt
) {

    public static ProjectDetailResponse from(ProjectEntity project) {

        return new ProjectDetailResponse(
                project.getProjectId(),
                project.getProjectName(),
                project.getDescription(),
                "https://github.com/"
                        + project.getGitRepoOwner()
                        + "/"
                        + project.getGitRepoName(),
                project.getGitRepoOwner(),
                project.getGitRepoName(),
                project.getDefaultBranch(),
                project.getCreatedAt()
        );
    }
}