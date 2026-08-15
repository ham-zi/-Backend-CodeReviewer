package com.reviewer.system.model.dto;

import com.reviewer.enums.ReviewTypeRole;
import com.reviewer.system.model.Entity.SystemPromptEntity;

public record SystemSettingResponse(ReviewTypeRole type, Long systemPromptId, String version, String prompt) {
	public static SystemSettingResponse from(SystemPromptEntity prompt) {
		return new SystemSettingResponse(prompt.getType(),
										 prompt.getSystemPromptId(),
										 prompt.getVersion(),
										 prompt.getPrompt());
	}
}
