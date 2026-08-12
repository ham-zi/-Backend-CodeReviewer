package com.reviewer.review.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BranchReviewRequest(@NotBlank(message="base-branch를 입력해주세요.") String baseBranch,
							@NotBlank(message="head-branch를 입력해주세요.") String headBranch, 
							@NotNull(message="프로젝트 ID를 입력해주세요") Long projectId) {
}
