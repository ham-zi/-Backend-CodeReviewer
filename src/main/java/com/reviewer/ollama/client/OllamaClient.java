package com.reviewer.ollama.client;

import java.time.Duration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.client.ReactorClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.reviewer.ai.client.AiReviewClient;
import com.reviewer.ai.model.dto.AiReviewResponse;
import com.reviewer.configuration.OllamaProperties;
import com.reviewer.ollama.model.dto.OllamaOptions;
import com.reviewer.ollama.model.dto.OllamaRequest;
import com.reviewer.ollama.model.dto.OllamaResponse;

import lombok.extern.slf4j.Slf4j;
import reactor.netty.http.client.HttpClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
@ConditionalOnProperty(
        name = "app.ai.provider",
        havingValue = "ollama",
        matchIfMissing = true
)
@Slf4j
public class OllamaClient implements AiReviewClient {

    private final RestClient restClient;
    private final OllamaProperties properties;
    private final ObjectMapper objectMapper;

    public OllamaClient(
            OllamaProperties properties,
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
        this.objectMapper = objectMapper;
    }

    @Override
    public AiReviewResponse review(
            String systemPrompt,
            String prompt,
            String formatSchema
    ) {

        JsonNode format;

        try {
            format = objectMapper.readTree(formatSchema);
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

        long startTime = System.nanoTime();

        OllamaResponse response = restClient.post()
                .uri("/api/generate")
                .body(request)
                .retrieve()
                .body(OllamaResponse.class);

        long responseTime = System.nanoTime() - startTime;

        if (response == null) {
            throw new IllegalStateException(
                    "Ollama 응답이 존재하지 않습니다."
            );
        }

        return new AiReviewResponse(
                response.model(),
                response.response(),
                response.promptEvalCount(),
                response.evalCount(),
                responseTime
        );
    }

    @Override
    public String getModel() {
        return properties.model();
    }
}
