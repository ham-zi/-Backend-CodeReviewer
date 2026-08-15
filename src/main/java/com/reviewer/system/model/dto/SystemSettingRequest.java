package com.reviewer.system.model.dto;

import com.reviewer.enums.ReviewTypeRole;

public record SystemSettingRequest(ReviewTypeRole type, Long systemPromptId) {

}
