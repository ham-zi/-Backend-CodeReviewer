package com.reviewer.review.model.service;

import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.reviewer.ai.client.AiReviewClient;
import com.reviewer.ai.model.dto.AiReviewResponse;
import com.reviewer.configuration.AiProperties;
import com.reviewer.review.model.dto.ReviewProcessData;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeamReviewProcessor {

    private final AiReviewClient aiReviewClient;
    private final AiProperties aiProperties;
    private final JsonMapper jsonMapper;
    private final ReviewTransactionService reviewTransactionService;

    public void process(
            Long reviewId,
            ReviewProcessData data
    ) {

        String formatSchema = createReviewFormatSchema(aiProperties.format());

        // 일반 코드 리뷰는 팀 규칙을 섞지 않고 코드 자체만 전달한다.
        AiReviewResponse generalResponse =
                aiReviewClient.review(
                        data.generalSystemPrompt(),
                        data.sourceCode(),
                        formatSchema
                );

        // 규칙 리뷰는 동일한 코드를 팀 규칙과 함께 전달한다.
        String rulePrompt = createRulePrompt(
                data.sourceCode(),
                data.ruleContent()
        );

        AiReviewResponse ruleResponse =
                aiReviewClient.review(
                        data.ruleSystemPrompt(),
                        rulePrompt,
                        formatSchema
                );

        try {
            JsonNode generalJson =
                    jsonMapper.readTree(generalResponse.response());

            JsonNode ruleJson =
                    jsonMapper.readTree(ruleResponse.response());

            reviewTransactionService.complete(
                    reviewId,
                    generalResponse,
                    ruleResponse,
                    generalJson,
                    ruleJson
            );

        } catch (Exception e) {
            throw new IllegalStateException(
                    "AI 리뷰 응답 JSON 파싱에 실패했습니다.",
                    e
            );
        }
    }

    private String createRulePrompt(
            String sourceCode,
            String ruleContent
    ) {

        return """
                ## 리뷰 대상 코드 ##
                %s

                ## 팀 규칙 ##
                %s
                """.formatted(
                        sourceCode,
                        ruleContent
                );
    }

    /**
     * 배포 환경에 남아 있는 기존 JSON Schema에도 새 분류와 라인 필드를 적용한다.
     */
    private String createReviewFormatSchema(String rawSchema) {
        try {
            JsonNode root = jsonMapper.readTree(rawSchema);
            ObjectNode schemaProperties = requireObject(root, "properties");
            ObjectNode reviews = requireObject(schemaProperties, "reviews");
            ObjectNode items = requireObject(reviews, "items");
            ObjectNode properties = requireObject(items, "properties");
            ObjectNode status = requireObject(properties, "status");

            ArrayNode statuses = jsonMapper.createArrayNode();
            statuses.add("RISK");
            statuses.add("WARNING");
            statuses.add("RECOMMENDATION");
            statuses.add("PASS");
            statuses.add("NOT_APPLICABLE");
            status.set("enum", statuses);

            ObjectNode filePath = jsonMapper.createObjectNode();
            filePath.put("type", "string");
            properties.set("filePath", filePath);

            ObjectNode startLine = jsonMapper.createObjectNode();
            startLine.put("type", "integer");
            startLine.put("minimum", 0);
            properties.set("startLine", startLine);

            Set<String> requiredNames = new LinkedHashSet<>();
            JsonNode currentRequired = items.get("required");
            if (currentRequired != null && currentRequired.isArray()) {
                for (JsonNode field : currentRequired) {
                    requiredNames.add(field.asText());
                }
            }
            requiredNames.add("filePath");
            requiredNames.add("startLine");

            ArrayNode required = jsonMapper.createArrayNode();
            requiredNames.forEach(required::add);
            items.set("required", required);

            return jsonMapper.writeValueAsString(root);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "AI 리뷰 JSON Schema에 심각도와 라인 필드를 적용할 수 없습니다.",
                    e
            );
        }
    }

    private ObjectNode requireObject(JsonNode parent, String fieldName) {
        JsonNode value = parent == null ? null : parent.get(fieldName);
        if (!(value instanceof ObjectNode objectNode)) {
            throw new IllegalStateException(
                    "AI 리뷰 JSON Schema 구조가 올바르지 않습니다: " + fieldName
            );
        }
        return objectNode;
    }
}
