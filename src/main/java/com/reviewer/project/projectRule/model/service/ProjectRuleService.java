package com.reviewer.project.projectRule.model.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.reviewer.auth.model.vo.CustomUserDetails;
import com.reviewer.exception.common.NotFoundException;
import com.reviewer.project.model.entity.ProjectEntity;
import com.reviewer.project.model.repository.ProjectRepository;
import com.reviewer.project.projectRule.model.dto.ProjectRuleDto;
import com.reviewer.project.projectRule.model.dto.RulesResponse;
import com.reviewer.project.projectRule.model.entity.ProjectRuleEntity;
import com.reviewer.project.projectRule.model.repository.ProjectRuleRepository;
import com.reviewer.project.validator.ProjectValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly=true)
public class ProjectRuleService {
	
	private final ProjectRepository projectRepository;
	private final ProjectRuleRepository projectRuleRepository;
	private final ProjectValidator projectValidator;
	
	@Transactional
	public void save(CustomUserDetails user, ProjectRuleDto rule, Long projectId) {
		projectValidator.checkProjectOwner(projectId, user.getUserId());
		ProjectRuleEntity entity = ProjectRuleEntity.of(projectRepository.findById(projectId).orElseThrow(()-> new NotFoundException("존재하지 않는 프로젝트입니다.")),
														rule.getTitle(), rule.getContent(), rule.getVersion());
		projectRuleRepository.save(entity);
	}

	public List<RulesResponse> findAll(CustomUserDetails user, Long projectId) {
		ProjectEntity project = projectValidator.checkProjectMember(projectId, user.getUserId());
		List<ProjectRuleEntity> rules = projectRuleRepository.findAllByProject(project);
		return rules.stream()
					.map(RulesResponse :: from)
					.toList();
	}
}
