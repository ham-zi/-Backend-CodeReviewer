package com.reviewer.review.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PrReviewRequest(
        @NotNull(message = "PR 번호를 입력해주세요.")
        @Positive(message = "PR 번호는 1 이상이어야 합니다.")
        Integer pullNumber,

        @NotNull(message = "프로젝트 ID를 입력해주세요.")
        Long projectId
) {
}
