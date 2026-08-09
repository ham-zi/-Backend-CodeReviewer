package com.reviewer.project.projectRule.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class ProjectRuleDto {
	
	private Long projectId;
    @NotBlank(message = "규칙 제목을 입력해야 합니다.")
    @Size(
        min = 1,
        max = 100,
        message = "규칙 제목은 1자 이상 100자 이하만 가능합니다."
    )
    private String title;

    @NotBlank(message = "규칙 내용을 입력해야 합니다.")
    private String content;

    @NotBlank(message = "버전을 입력해야 합니다.")
    @Size(
        min = 1,
        max = 20,
        message = "버전은 1자 이상 20자 이하만 가능합니다."
    )
    @Pattern(
        regexp = "^\\d+\\.\\d+$",
        message = "버전은 1.0, 1.1과 같은 형식이어야 합니다."
    )
    private String version;
}