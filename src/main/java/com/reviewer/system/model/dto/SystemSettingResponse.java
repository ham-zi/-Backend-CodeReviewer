package com.reviewer.system.model.dto;

import com.reviewer.enums.ReviewTypeRole;
import com.reviewer.system.model.Entity.SystemPromptEntity;

public record SystemSettingResponse(
        ReviewTypeRole type,
        Long systemPromptId,
        String version,
        String generalPrompt,
        String rulePrompt
) {

    public static SystemSettingResponse from(SystemPromptEntity prompt) {
        return new SystemSettingResponse(
                prompt.getType(),
                prompt.getSystemPromptId(),
                prompt.getVersion(),
                prompt.getGeneralPrompt(),
                prompt.getRulePrompt()
        );
    }
}