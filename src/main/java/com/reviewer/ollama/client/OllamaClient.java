package com.reviewer.ollama.client;
import java.time.Duration;

import org.springframework.http.client.ReactorClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.reviewer.configuration.OllamaProperties;
import com.reviewer.enums.ReviewTypeRole;
import com.reviewer.ollama.model.dto.OllamaOptions;
import com.reviewer.ollama.model.dto.OllamaRequest;
import com.reviewer.ollama.model.dto.OllamaResponse;
import com.reviewer.system.model.service.SystemPromptService;

import lombok.extern.slf4j.Slf4j;
import reactor.netty.http.client.HttpClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
@Slf4j
public class OllamaClient {

    private final RestClient restClient;
    private final OllamaProperties properties;
    private final SystemPromptService systemPromptService;
    private final ObjectMapper objectMapper;

    public OllamaClient(
            OllamaProperties properties,
            SystemPromptService systemPromptService,
            ObjectMapper objectMapper
    ) {

        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofMinutes(30));

        ReactorClientHttpRequestFactory requestFactory =
                new ReactorClientHttpRequestFactory(httpClient);

        this.restClient = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .requestFactory(requestFactory)
                .build();

        this.properties = properties;
        this.systemPromptService = systemPromptService;
        this.objectMapper = objectMapper;
    }

    public OllamaResponse quickReview(String prompt) {
        return review(prompt, ReviewTypeRole.QUICK);
    }

    public OllamaResponse branchReview(String prompt) {
        return review(prompt, ReviewTypeRole.BRANCH);
    }

    private OllamaResponse review(
            String prompt,
            ReviewTypeRole reviewType
    ) {

        String systemPrompt = systemPromptService.findCurrentPrompt(reviewType);
        
        JsonNode format;

        try {
            format = objectMapper.readTree(properties.format());
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Ollama JSON Schema 파싱 실패",
                    e
            );
        }

        OllamaRequest request = new OllamaRequest(
                properties.model(),
                systemPrompt,
                prompt,
                properties.stream(),
                format,
                new OllamaOptions(
                        properties.numCtx(),
                        properties.temperature()
                )
        );
        OllamaResponse response = restClient.post()
                .uri("/api/generate")
                .body(request)
                .retrieve()
                .body(OllamaResponse.class);

        if (response == null) {
            throw new IllegalStateException(
                    "Ollama 응답이 존재하지 않습니다."
            );
        }

        return response;
    }
}