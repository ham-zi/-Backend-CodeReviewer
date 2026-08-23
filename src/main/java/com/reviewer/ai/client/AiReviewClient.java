package com.reviewer.ai.client;

import com.reviewer.ai.model.dto.AiReviewResponse;

public interface AiReviewClient {

    AiReviewResponse review(
            String systemPrompt,
            String prompt,
            String formatSchema
    );

    String getModel();
}
