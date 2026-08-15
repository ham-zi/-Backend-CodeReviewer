package com.reviewer.project.projectRule.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reviewer.api.model.vo.ApiResponse;
import com.reviewer.auth.model.vo.CustomUserDetails;
import com.reviewer.project.projectRule.model.dto.ProjectRuleDto;
import com.reviewer.project.projectRule.model.dto.RulesResponse;
import com.reviewer.project.projectRule.model.service.ProjectRuleService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rules")
@Slf4j
public class ProjectRuleController {
	
	private final ProjectRuleService projectRuleService;
	
	@PostMapping("/{projectId}")
	public ResponseEntity<ApiResponse<Void>> saveProjectRule(@AuthenticationPrincipal CustomUserDetails user,
															 @RequestBody ProjectRuleDto rule,
															 @PathVariable(name="projectId")Long projectId){
		projectRuleService.save(user, rule, projectId);
		return ResponseEntity.ok(ApiResponse.success("팀 컨벤션 생성에 성공했습니다.", null));
	}
	
	@GetMapping("/{projectId}")
	public ResponseEntity<ApiResponse<List<RulesResponse>>> findProjectRules(@AuthenticationPrincipal CustomUserDetails user,
															  @PathVariable(name="projectId")Long projectId) {
		return ResponseEntity.ok(ApiResponse.success("팀 규칙 목록조회에 성공했습니다.", projectRuleService.findAll(user, projectId)));
	}
	
}
