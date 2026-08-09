package com.reviewer.project.validator;

import java.util.List;

import org.springframework.stereotype.Component;

import com.reviewer.exception.common.NotFoundException;
import com.reviewer.project.model.entity.ProjectEntity;
import com.reviewer.project.model.repository.ProjectRepository;
import com.reviewer.project.projectMember.model.Entity.ProjectMemberEntity;
import com.reviewer.project.projectMember.model.repository.ProjectMemberRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProjectValidator {
	private final ProjectRepository projectRepository;
	private final ProjectMemberRepository projectMemberRepository;
	
	public void checkProjectMember(Long projectId, Long userId) {
		if(!projectMemberRepository.existsByProjectAndUser(projectId, userId)) {
			throw new NotFoundException("존재하지 않는 멤버입니다.");
		}
		
	}
	
}
