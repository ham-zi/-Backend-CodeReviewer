package com.reviewer.openai.client;

import java.time.Duration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ReactorClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.reviewer.ai.client.AiReviewClient;
import com.reviewer.ai.model.dto.AiReviewResponse;
import com.reviewer.configuration.OpenAiProperties;
import com.reviewer.openai.model.dto.OpenAiRequest;
import com.reviewer.openai.model.dto.OpenAiRequest.Format;
import com.reviewer.openai.model.dto.OpenAiRequest.TextConfig;

import lombok.extern.slf4j.Slf4j;
import reactor.netty.http.client.HttpClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
@ConditionalOnProperty(
        name = "app.ai.provider",
        havingValue = "openai"
)
@Slf4j
public class OpenAiClient implements AiReviewClient {

    private final RestClient restClient;
    private final OpenAiProperties properties;
    private final ObjectMapper objectMapper;

    public OpenAiClient(
            OpenAiProperties properties,
            ObjectMapper objectMapper
    ) {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofMinutes(10));

        ReactorClientHttpRequestFactory requestFactory =
                new ReactorClientHttpRequestFactory(httpClient);

        this.restClient = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .requestFactory(requestFactory)
                .defaultHeader(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + properties.apiKey()
                )
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

        JsonNode schema;

        try {
            schema = objectMapper.readTree(formatSchema);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "OpenAI JSON Schema 파싱 실패",
                    e
            );
        }

        /*
         * OpenAI Responses API의 Structured Outputs 형식이다.
         * 기존 Ollama에서 사용하던 JSON Schema를 그대로 schema에 넣고,
         * strict=true로 설정하여 두 Provider가 같은 응답 JSON 구조를 사용하도록 한다.
         */
        OpenAiRequest request = new OpenAiRequest(
                properties.model(),
                systemPrompt,
                prompt,
                false,
                new TextConfig(
                        new Format(
                                "json_schema",
                                "review_response",
                                true,
                                schema
                        )
                )
        );

        long startTime = System.nanoTime();

        String responseBody = restClient.post()
                .uri("/v1/responses")
                .body(request)
                .retrieve()
                .body(String.class);

        long responseTime = System.nanoTime() - startTime;

        if (responseBody == null) {
            throw new IllegalStateException(
                    "OpenAI 응답이 존재하지 않습니다."
            );
        }

        try {
            JsonNode root = objectMapper.readTree(responseBody);

            String status = getText(root, "status", "");
            if (!"completed".equals(status)) {
                throw new IllegalStateException(
                        "OpenAI 응답이 정상 완료되지 않았습니다. status=" + status
                );
            }

            String responseText = findOutputText(root);

            JsonNode usage = root.get("usage");
            Integer inputTokens = getInteger(usage, "input_tokens");
            Integer outputTokens = getInteger(usage, "output_tokens");

            return new AiReviewResponse(
                    getText(root, "model", properties.model()),
                    responseText,
                    inputTokens,
                    outputTokens,
                    responseTime
            );

        } catch (Exception e) {
            throw new IllegalStateException(
                    "OpenAI 응답 파싱에 실패했습니다.",
                    e
            );
        }
    }

    private String findOutputText(JsonNode root) {

        JsonNode output = root.get("output");

        if (output == null || !output.isArray()) {
            throw new IllegalStateException(
                    "OpenAI 응답에 output 배열이 존재하지 않습니다."
            );
        }

        for (JsonNode item : output) {
            JsonNode content = item.get("content");

            if (content == null || !content.isArray()) {
                continue;
            }

            for (JsonNode value : content) {
                JsonNode type = value.get("type");
                JsonNode text = value.get("text");

                if (type != null &&
                    "output_text".equals(type.asText()) &&
                    text != null) {
                    return text.asText();
                }
            }
        }

        throw new IllegalStateException(
                "OpenAI 응답에 output_text가 존재하지 않습니다."
        );
    }

    private Integer getInteger(JsonNode parent, String fieldName) {
        if (parent == null || parent.get(fieldName) == null) {
            return null;
        }
        return parent.get(fieldName).asInt();
    }

    private String getText(
            JsonNode parent,
            String fieldName,
            String defaultValue
    ) {
        if (parent == null || parent.get(fieldName) == null) {
            return defaultValue;
        }
        return parent.get(fieldName).asText();
    }

    @Override
    public String getModel() {
        return properties.model();
    }
}
