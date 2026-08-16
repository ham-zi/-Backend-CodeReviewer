package com.reviewer.project.model.dto;

import java.time.Instant;

import com.reviewer.project.model.entity.ProjectEntity;

public record ProjectListResponse(
        Long projectId,
        String projectName,
        String description,
        String repositoryUrl,
        String defaultBranch,
        Instant createdAt
) {

    public static ProjectListResponse from(ProjectEntity project) {
        return new ProjectListResponse(
                project.getProjectId(),
                project.getProjectName(),
                project.getDescription(),
                "https://github.com/"+project.getGitRepoOwner()+"/"+project.getGitRepoName(),
                project.getDefaultBranch(),
                project.getCreatedAt()
        );
    }
}