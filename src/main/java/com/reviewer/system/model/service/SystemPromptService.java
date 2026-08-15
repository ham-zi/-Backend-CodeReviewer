package com.reviewer.system.model.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.reviewer.auth.model.vo.CustomUserDetails;
import com.reviewer.enums.ReviewTypeRole;
import com.reviewer.exception.common.DuplicateException;
import com.reviewer.system.model.Entity.SystemPromptEntity;
import com.reviewer.system.model.dao.SystemPromptRepository;
import com.reviewer.system.model.dto.SystemPromptDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly=true)
public class SystemPromptService {
	
	private final SystemPromptRepository systemPromptRepository;
	
	@Transactional
	public void save(CustomUserDetails user, SystemPromptDto prompt) {
		ValidateVersion(prompt.getType(), prompt.getVersion());
		systemPromptRepository.save(SystemPromptEntity.of(prompt.getPrompt(),
														  prompt.getVersion(),
														  prompt.getImprovement(),
														  prompt.getType()));
	}
	
	public List<SytemPromptEntity> findAll(){
		
	}
	
	private void ValidateVersion(ReviewTypeRole type, String version) {
		if(systemPromptRepository.existsByTypeAndVersion(type , version)) {
			throw new DuplicateException("버전명이 중복되었습니다.");
		}
	}
}
