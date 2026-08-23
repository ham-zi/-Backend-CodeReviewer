package com.reviewer.openai.model.dto;

import tools.jackson.databind.JsonNode;

public record OpenAiRequest(
        String model,
        String instructions,
        String input,
        boolean store,
        TextConfig text
) {

    public record TextConfig(Format format) {
    }

    public record Format(
            String type,
            String name,
            boolean strict,
            JsonNode schema
    ) {
    }
}
