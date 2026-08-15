package com.reviewer.system.model.service;




import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.reviewer.enums.ReviewTypeRole;
import com.reviewer.exception.common.DuplicateException;
import com.reviewer.exception.common.NotFoundException;
import com.reviewer.system.model.Entity.SystemPromptEntity;
import com.reviewer.system.model.Entity.SystemSettingEntity;
import com.reviewer.system.model.dao.SystemPromptRepository;
import com.reviewer.system.model.dao.SystemSettingRepository;
import com.reviewer.system.model.dto.SystemPromptDto;
import com.reviewer.system.model.dto.SystemPromptResponse;
import com.reviewer.system.model.dto.SystemSettingRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly=true)
public class SystemPromptService {
	
	private final SystemPromptRepository systemPromptRepository;
	private final SystemSettingRepository systemSettingRepository;
	
	@Transactional
	public void save(SystemPromptDto prompt) {
		ValidateVersion(prompt.getType(), prompt.getVersion());
		systemPromptRepository.save(SystemPromptEntity.of(prompt.getPrompt(),
														  prompt.getVersion(),
														  prompt.getImprovement(),
														  prompt.getType()));
	}
	
	public Page<SystemPromptResponse> findAll(int page, ReviewTypeRole type){
		Pageable pageable = (Pageable) PageRequest.of(page - 1,
													  10,
													  Sort.by(Sort.Direction.DESC, "createdAt"));
		Page<SystemPromptEntity> prompts = systemPromptRepository.findAllByType(type, pageable);
		return prompts.map(SystemPromptResponse::from);
	}
	
	public SystemPromptResponse findById(Long systemPromptId) {
		SystemPromptEntity prompt = systemPromptRepository.findById(systemPromptId).orElseThrow(() ->new NotFoundException("존재하지 않는 시스템프롬프트입니다."));
		return SystemPromptResponse.from(prompt);
	}
	
	@Transactional
	public void delete(Long systemPromptId) {
		systemPromptRepository.deleteById(systemPromptId);
	}
	
	private void ValidateVersion(ReviewTypeRole type, String version) {
		if(systemPromptRepository.existsByTypeAndVersion(type , version)) {
			throw new DuplicateException("버전명이 중복되었습니다.");
		}
	}
	
	@Transactional
	public void patchSetting(SystemSettingRequest settings) {
		SystemSettingEntity setting = systemSettingRepository.findById(settings.type()).orElseThrow(()-> new NotFoundException("존재하지 않는 리뷰타입입니다."));
		setting.changePrompt(systemPromptRepository.findById(settings.systemPromptId()).orElseThrow(()-> new NotFoundException("존재하지 않는 시스템프롬프트입니다.")));
	}
	
	
}
