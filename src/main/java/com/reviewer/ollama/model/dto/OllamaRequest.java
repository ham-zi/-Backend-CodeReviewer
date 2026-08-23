package com.reviewer.ollama.model.dto;

import tools.jackson.databind.JsonNode;

public record OllamaRequest(String model, String system, String prompt, boolean stream, JsonNode format , OllamaOptions options) {

}
