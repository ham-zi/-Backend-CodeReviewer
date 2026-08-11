package com.reviewer.ollama.model.dto;

import java.util.Map;

public record OllamaRequest(String model, String system, String prompt, boolean stream, String format , Map<String, Object> options) {

}
