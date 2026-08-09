package com.reviewer.project.model.entity;

import java.time.Instant;

import com.reviewer.project.projectRule.model.entity.ProjectRuleEntity;
import com.reviewer.user.model.entity.UserEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "PROJECT", uniqueConstraints = {
		@UniqueConstraint(name = "UK_PE_REPO_URL", columnNames = "REPOSITORY_URL")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ProjectEntity {
	
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long projectId;
	@Column(name = "PROJECT_NAME", nullable = false, length = 40)
	private String projectName;
	@Column(name = "DESCRIPTION", length = 400)	
	private String description;
	@Column(name = "GIT_REPO_OWNER", nullable = false, length = 100)
	private String gitRepoOwner;
	@Column(name = "GIT_REPO_NAME", nullable = false, length = 100)
	private String gitRepoName;
	@Column(name = "DEFAULT_BRANCH", nullable = false, length = 100)
	private String defaultBranch;
	@JoinColumn(name = "USER_ID", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private UserEntity createdBy;
	@Column(name = "CREATED_AT", nullable = false, updatable = false)
	private Instant createdAt;
	@Column(name = "UPDATED_AT")
	private Instant updatedAt;
	@JoinColumn(name = "RULE_ID")
	@ManyToOne(fetch = FetchType.LAZY)
	private ProjectRuleEntity projectRule;
	
	private ProjectEntity(String projectName,
			              String description,
			              String gitRepoOwner,
			              String gitRepoName,
			              String defaultBranch,
			              UserEntity createdBy) {		
		this.projectName = projectName;
		this.description = description;
		this.gitRepoOwner = gitRepoOwner;
		this.gitRepoName = gitRepoName;
		this.defaultBranch = defaultBranch;
		this.createdBy = createdBy;
	}
	
	public static ProjectEntity of(String projectName,
			              String description,
			              String gitRepoOwner,
			              String gitRepoName,
			              String defaultBranch,
			              UserEntity createdBy) {
		return new ProjectEntity(projectName, description, gitRepoOwner, gitRepoName, defaultBranch, createdBy);
	}
	
	@PrePersist
	private void onCreated() {
		Instant now = Instant.now();
			createdAt = now;
			updatedAt = now;
	}

	@PreUpdate
	private void onUpdated() {
		updatedAt = Instant.now();
	}
	
	public void changeProjectRule(ProjectRuleEntity rule) {
		if(!rule.getProject().equals(this)) {
			throw new IllegalArgumentException("다른 프로젝트의 규칙은 적용 할 수 없습니다.");
		}
		this.projectRule = rule;
	}
	
	
}
