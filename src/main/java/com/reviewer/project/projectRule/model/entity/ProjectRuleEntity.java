package com.reviewer.project.projectRule.model.entity;

import java.time.Instant;

import com.reviewer.project.model.entity.ProjectEntity;

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
@Table(name = "PROJECT_RULE", uniqueConstraints = {
		@UniqueConstraint(name = "UK_PR_VERSION", columnNames = {"PROJECT_ID", "VERSION"})
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ProjectRuleEntity {
	
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long ruleId;
	@JoinColumn(name="PROJECT_ID", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private ProjectEntity project;
	@Column(name = "TITLE", nullable = false, length = 100)
	private String title;
	@Column(name = "CONTENT", nullable = false, columnDefinition = "TEXT")
	private String content;
	@Column(name = "VERSION", nullable = false, length = 20)
	private String version;
	@Column(name = "CREATED_AT", nullable = false, updatable = false)
	private Instant createdAt;
	@Column(name = "UPDATED_AT")
	private Instant updatedAt;
	
	private ProjectRuleEntity(ProjectEntity project,
			                  String title,
			                  String content,
			                  String version) {
		this.project = project;
		this.title = title;
		this.content = content;
		this.version = version;
	}
	
	public static ProjectRuleEntity of(ProjectEntity project,
							           String title,
							           String content,
							           String version) {
		return new ProjectRuleEntity(project, title, content, version);
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
	
}
