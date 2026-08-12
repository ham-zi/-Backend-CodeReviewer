package com.reviewer.review.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record QuickReviewRequest(@NotBlank(message="코드를 입력해주세요.") String code, @NotNull(message="프로젝트ID를 입력해주세요.") Long projectId) {

}
