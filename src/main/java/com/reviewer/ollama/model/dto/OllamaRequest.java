package com.reviewer.ollama.model.dto;

public record OllamaRequest(String model, String system, String prompt, boolean stream, String format , OllamaOptions options) {

}
