package com.reviewer.system.model.dto;

import java.time.Instant;

import com.reviewer.enums.ReviewTypeRole;
import com.reviewer.system.model.Entity.SystemPromptEntity;

public record SystemPromptResponse(Long systemPromptId,
								   String generalPrompt,
								   String rulePrompt,
								   String version,
								   String improvement,
								   ReviewTypeRole type,
								   Instant createdAt) {
	public static SystemPromptResponse from(SystemPromptEntity prompt) {
		return new SystemPromptResponse(prompt.getSystemPromptId(),
										prompt.getGeneralPrompt(),
										prompt.getRulePrompt(),
										prompt.getVersion(),
										prompt.getImprovement(),
										prompt.getType(),
										prompt.getCreatedAt()
				);
	}
}
