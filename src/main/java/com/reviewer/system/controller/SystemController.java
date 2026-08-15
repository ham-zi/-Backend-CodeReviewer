package com.reviewer.system.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.reviewer.api.model.vo.ApiResponse;
import com.reviewer.enums.ReviewTypeRole;
import com.reviewer.system.model.dto.SystemPromptDto;
import com.reviewer.system.model.dto.SystemPromptResponse;
import com.reviewer.system.model.service.SystemPromptService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/systems")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class SystemController {
	
	private final SystemPromptService systemPromptService;
	
	@PostMapping
	public ResponseEntity<ApiResponse<Void>> save(@Valid @RequestBody SystemPromptDto prompt) {
		systemPromptService.save(prompt);
		return ResponseEntity.ok(ApiResponse.created("시스템 프롬프트 생성에 성공했습니다.", null));
	}
	
	@GetMapping
	public ResponseEntity<ApiResponse<Page<SystemPromptResponse>>> findAll(@RequestParam(name="page", defaultValue="1")int page,
																		   @RequestParam(name="type") ReviewTypeRole type) {
		return ResponseEntity.ok(ApiResponse.success("시스템 프롬프트 목록조회에 성공했습니다.", systemPromptService.findAll(page, type)));
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<SystemPromptResponse>> findById(@PathVariable(name="id") Long id) {
		return ResponseEntity.ok(ApiResponse.success("시스템 프롬프트 상세조회에 성공했습니다.", systemPromptService.findById(id)));
	}
	
	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse<Void>> delete(@PathVariable(name="id")Long id) {
		systemPromptService.delete(id);
		return ResponseEntity.ok(ApiResponse.success("시스템 프롬프트 삭제에 성공했습니다.", null));
	}
	
}
