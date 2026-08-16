package com.reviewer.project.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.reviewer.api.model.vo.ApiResponse;
import com.reviewer.auth.model.vo.CustomUserDetails;
import com.reviewer.github.model.dto.BranchResponse;
import com.reviewer.github.model.service.GithubService;
import com.reviewer.project.model.dto.ProjectDetailResponse;
import com.reviewer.project.model.dto.ProjectDto;
import com.reviewer.project.model.dto.ProjectListResponse;
import com.reviewer.project.model.service.ProjectService;
import com.reviewer.project.projectMember.model.service.ProjectMemberService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/projects")
@Slf4j
public class ProjectController {

	private final GithubService githubService;
	private final ProjectService projectService;
	private final ProjectMemberService projectMemberService;
	
	@PostMapping
	public ResponseEntity<ApiResponse<Void>> saveProject(@Valid @RequestBody ProjectDto project,
														 @AuthenticationPrincipal CustomUserDetails user) {
		projectService.saveProject(user, project);
		return ResponseEntity.ok(ApiResponse.created("프로젝트 저장에 성공했습니다.", null)); 
	}
	
	@PatchMapping("/{projectId}/rule/{ruleId}")
	public ResponseEntity<ApiResponse<Void>> updateProjectRule(@PathVariable(name="projectId")Long projectId,
															   @PathVariable(name="ruleId")Long ruleId,
															   @AuthenticationPrincipal CustomUserDetails user) {
		projectService.updateProjectRule(projectId, ruleId, user);
		return ResponseEntity.ok(ApiResponse.success("프로젝트 규칙 변경에 성공했습니다.", null));
	}

	
	@GetMapping("/{projectId}/git/branches")
	public ResponseEntity<ApiResponse<List<BranchResponse>>> getBranches(@PathVariable(name = "projectId")Long projectId,
			 															 @AuthenticationPrincipal CustomUserDetails user) {
		return ResponseEntity.ok(ApiResponse.success("브랜치 조회에 성공했습니다.", githubService.getBranches(projectId, user)));
	}
	
	@GetMapping("/{projectId}")
	public ResponseEntity<ApiResponse<ProjectDetailResponse>> findByProjectId(@AuthenticationPrincipal CustomUserDetails user,
																			  @PathVariable(name="projectId") Long projectId) {
		return ResponseEntity.ok(ApiResponse.success("프로젝트 상세조회에 성공했습니다.", projectService.findByProjectId(user, projectId)));
	}
	
	@GetMapping
	public ResponseEntity<ApiResponse<Page<ProjectListResponse>>> findAllByUserId(@AuthenticationPrincipal CustomUserDetails user, @RequestParam(name="page", defaultValue="1") int page) {
		return ResponseEntity.ok(ApiResponse.success("프로젝트 목록조회에 성공했습니다.", projectMemberService.findAllByUserId(user, page)));
	}
	
}
