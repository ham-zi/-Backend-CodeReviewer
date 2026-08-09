package com.reviewer.project.projectMember.model.Entity;

import java.time.Instant;

import com.reviewer.enums.ProjectMemberRole;
import com.reviewer.project.model.entity.ProjectEntity;
import com.reviewer.user.model.entity.UserEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "PROJECT_MEMBER",
       uniqueConstraints = {
    		   @UniqueConstraint(name = "UK_PM_PI_UI", columnNames = {"PROJECT_ID", "USER_ID"})
       }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ProjectMemberEntity {
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "PROJECT_MEMBER_ID")
	private Long projectMemberId;
	@JoinColumn(name = "PROJECT_ID", nullable = false)
	@ManyToOne(fetch=FetchType.LAZY)
	private ProjectEntity project;
	@JoinColumn(name = "USER_ID", nullable = false)
	@ManyToOne(fetch=FetchType.LAZY)
	private UserEntity user;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private ProjectMemberRole projectMemberRole;
	@Column(name = "JOINED_AT", nullable = false, updatable = false)
	private Instant joinedAt;
	
	
	private ProjectMemberEntity(ProjectEntity project, UserEntity user, ProjectMemberRole role) {
		this.project = project;
		this.user = user;
		this.projectMemberRole = role;
	}
	
	public static ProjectMemberEntity of(ProjectEntity project, UserEntity user, ProjectMemberRole role) {
		return new ProjectMemberEntity(project, user, role);
	}
	
	@PrePersist
	private void onJoined() {
		joinedAt = Instant.now();
	}
	
	
	
	
}
