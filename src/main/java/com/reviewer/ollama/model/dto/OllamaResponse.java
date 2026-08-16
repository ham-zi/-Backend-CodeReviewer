package com.reviewer.ollama.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OllamaResponse(

        String model,

        @JsonProperty("created_at")
        String createdAt,

        String response,

        Boolean done,

        @JsonProperty("done_reason")
        String doneReason,

        @JsonProperty("total_duration")
        Long totalDuration,

        @JsonProperty("load_duration")
        Long loadDuration,

        @JsonProperty("prompt_eval_count")
        Integer promptEvalCount,

        @JsonProperty("prompt_eval_duration")
        Long promptEvalDuration,

        @JsonProperty("eval_count")
        Integer evalCount,

        @JsonProperty("eval_duration")
        Long evalDuration

) {
}