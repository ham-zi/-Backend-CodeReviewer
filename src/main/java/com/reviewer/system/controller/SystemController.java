package com.reviewer.system.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reviewer.api.model.vo.ApiResponse;
import com.reviewer.auth.model.vo.CustomUserDetails;
import com.reviewer.system.model.dto.SystemPromptDto;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/systems")
@RequiredArgsConstructor
@Slf4j
public class SystemController {
	
	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<Void>> save(@AuthenticationPrincipal CustomUserDetails user,
												  @Valid @RequestBody SystemPromptDto prompt) {
		return ResponseEntity.ok(ApiResponse.created("시스템 프롬프트 생성에 성공했습니다.", null));
	}
	
}
