package com.reviewer.project.projectMember.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProjectMemberCreateRequest(
        @NotBlank(message = "추가할 사용자의 로그인 ID를 입력해주세요.")
        @Size(max = 20, message = "로그인 ID는 20자 이하여야 합니다.")
        String loginId
) {
}
