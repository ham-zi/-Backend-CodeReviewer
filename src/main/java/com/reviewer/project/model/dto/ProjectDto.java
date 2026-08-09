package com.reviewer.project.model.dto;

import java.time.Instant;

import com.reviewer.project.projectRule.model.entity.ProjectRuleEntity;
import com.reviewer.user.model.entity.UserEntity;

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
public class ProjectDto {
	private Long projectId;
	@Pattern(regexp = "^[a-zA-Z-가-힣0-9!@#$%^&*()_-]+$", message="프로젝트 명은 영어/한글/숫자만 가능합니다.")
	@Size(min = 1, max = 40, message="프로젝트명 1글자이상 40글자 이하만 가능합니다.")
	@NotBlank(message = "프로젝트명을 입력 해야합니다.")
	private String projectName;
    private String description;
	@NotBlank(message = "해당 Github Repository의 아이디를 입력 해야합니다.")
    private String gitRepoOwner;
	@NotBlank(message = "해당 Github Repository의 저장소 이름를 입력 해야합니다.")
    private String gitRepoName;
	@NotBlank(message = "기본 브런치를 입력 해야합니다.")
    private String defaultBranch;
	private Long createdBy;
	private Instant createdAt;
	private Instant updatedAt;
	private ProjectRuleEntity projectRule;
}
