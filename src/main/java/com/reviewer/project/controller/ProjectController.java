package com.reviewer.project.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
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
import com.reviewer.github.model.dto.PullRequestResponse;
import com.reviewer.github.model.service.GithubService;
import com.reviewer.project.model.dto.ProjectDetailResponse;
import com.reviewer.project.model.dto.ProjectDto;
import com.reviewer.project.model.dto.ProjectListResponse;
import com.reviewer.project.model.service.ProjectService;
import com.reviewer.project.projectMember.model.dto.ProjectMemberCreateRequest;
import com.reviewer.project.projectMember.model.dto.ProjectMemberResponse;
import com.reviewer.project.projectMember.model.service.ProjectMemberService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

	private final GithubService githubService;
	private final ProjectService projectService;
	private final ProjectMemberService projectMemberService;

	public ProjectController(
			GithubService githubService,
			ProjectService projectService,
			ProjectMemberService projectMemberService) {
		this.githubService = githubService;
		this.projectService = projectService;
		this.projectMemberService = projectMemberService;
	}
	
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

	

	@GetMapping("/{projectId}/members")
	public ResponseEntity<ApiResponse<List<ProjectMemberResponse>>> findProjectMembers(
			@PathVariable(name = "projectId") Long projectId,
			@AuthenticationPrincipal CustomUserDetails user) {
		return ResponseEntity.ok(ApiResponse.success(
				"프로젝트 팀원 조회에 성공했습니다.",
				projectMemberService.findAllMembers(projectId, user)
		));
	}

	@PostMapping("/{projectId}/members")
	public ResponseEntity<ApiResponse<ProjectMemberResponse>> addProjectMember(
			@PathVariable(name = "projectId") Long projectId,
			@Valid @RequestBody ProjectMemberCreateRequest request,
			@AuthenticationPrincipal CustomUserDetails user) {
		return ResponseEntity.ok(ApiResponse.created(
				"프로젝트 팀원 추가에 성공했습니다.",
				projectMemberService.addMember(projectId, request, user)
		));
	}

	@DeleteMapping("/{projectId}/members/{projectMemberId}")
	public ResponseEntity<ApiResponse<Void>> removeProjectMember(
			@PathVariable(name = "projectId") Long projectId,
			@PathVariable(name = "projectMemberId") Long projectMemberId,
			@AuthenticationPrincipal CustomUserDetails user) {
		projectMemberService.removeMember(projectId, projectMemberId, user);
		return ResponseEntity.ok(ApiResponse.noContent("프로젝트 팀원 삭제에 성공했습니다.", null));
	}

	@GetMapping("/{projectId}/git/pulls")
	public ResponseEntity<ApiResponse<List<PullRequestResponse>>> getPullRequests(
			@PathVariable(name = "projectId") Long projectId,
			@AuthenticationPrincipal CustomUserDetails user) {

		return ResponseEntity.ok(
				ApiResponse.success(
						"PR 목록 조회에 성공했습니다.",
						githubService.getPullRequests(projectId, user)
				)
		);
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
