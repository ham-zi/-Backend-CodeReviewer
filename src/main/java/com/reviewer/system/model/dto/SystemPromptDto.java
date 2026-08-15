package com.reviewer.system.model.dto;

import com.reviewer.enums.ReviewTypeRole;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SystemPromptDto {
	@NotBlank(message="프롬프트를 적어주세요.")
	@Size(max = 1000, message = "개선점은 1000자 이하로 입력해주세요.")
	private String prompt;
	@NotBlank(message="버전을 적어주세요.")
	@Size(max = 20, message = "버전은 20자 이하로 입력해주세요.")
	private String version;
	@NotBlank(message="개선점을 적어주세요.")
	private String improvement;
	@NotNull(message="리뷰종류를 적어주세요.")
	private ReviewTypeRole type;
}
