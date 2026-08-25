package com.reviewer.project.validator;

import org.springframework.stereotype.Component;

import com.reviewer.enums.ProjectMemberRole;
import com.reviewer.exception.common.AccessDeniedException;
import com.reviewer.exception.common.NotFoundException;
import com.reviewer.project.model.entity.ProjectEntity;
import com.reviewer.project.model.repository.ProjectRepository;
import com.reviewer.project.projectMember.model.Entity.ProjectMemberEntity;
import com.reviewer.project.projectMember.model.repository.ProjectMemberRepository;
import com.reviewer.project.projectRule.model.entity.ProjectRuleEntity;
import com.reviewer.project.projectRule.model.repository.ProjectRuleRepository;
import com.reviewer.user.model.entity.UserEntity;
import com.reviewer.user.model.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProjectValidator {
	private final ProjectRepository projectRepository;
	private final UserRepository userRepository;
	private final ProjectMemberRepository projectMemberRepository;
	private final ProjectRuleRepository projectRuleRepository;
	
	public ProjectEntity existsProject(Long projectId) {
		return projectRepository.findById(projectId).orElseThrow(()-> new NotFoundException("존재하지 않는 프로젝트입니다."));
	}
	
	public ProjectEntity checkProjectMember(Long projectId, Long userId) {
		ProjectEntity project = existsProject(projectId);
		UserEntity user = userRepository.findById(userId).orElseThrow(()-> new NotFoundException("존재하지 않는 아이디입니다."));
		if(!projectMemberRepository.existsByProjectAndUser(project, user)) {
			throw new NotFoundException("존재하지 않는 멤버입니다.");
		}
		return project;
	}
	
	public void checkProjectOwner(Long projectId, Long userId) {
		ProjectMemberEntity member = projectMemberRepository.findByProjectAndProjectMemberRole(projectRepository.findById(projectId).orElseThrow(()-> new NotFoundException("프로젝트가 존재하지 않습니다.")),
																					 ProjectMemberRole.OWNER).orElseThrow(()-> new NotFoundException("Owner가 존재하지 않습니다."));
		if(!member.getUser().getId().equals(userId)) {
			throw new AccessDeniedException("프로젝트 관리 권한이 부족합니다.");
		}
	}
	
	public void checkRuleToProject(ProjectEntity project, ProjectRuleEntity rule) {
		if(!project.getProjectId().equals(rule.getProject().getProjectId())) {
			throw new AccessDeniedException("해당 팀의 팀컨벤션이 아닙니다.");
		}
		
	}
	
	public ProjectRuleEntity existsRule(Long ruleId) {
		return projectRuleRepository.findById(ruleId).orElseThrow(()-> new NotFoundException("존재하지 않는 팀컨벤션입니다."));
	}
	
}
