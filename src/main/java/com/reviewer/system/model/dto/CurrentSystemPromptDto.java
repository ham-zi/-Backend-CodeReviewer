package com.reviewer.system.model.dto;

import com.reviewer.system.model.Entity.SystemPromptEntity;

public record CurrentSystemPromptDto(
        String generalPrompt,
        String rulePrompt
) {

    public static CurrentSystemPromptDto from(SystemPromptEntity entity) {
        return new CurrentSystemPromptDto(
                entity.getGeneralPrompt(),
                entity.getRulePrompt()
        );
    }
}