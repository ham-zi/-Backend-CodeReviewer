package com.reviewer.review.model.service;

import org.springframework.stereotype.Service;

import com.reviewer.ai.client.AiReviewClient;
import com.reviewer.ai.model.dto.AiReviewResponse;
import com.reviewer.configuration.AiProperties;
import com.reviewer.review.model.dto.ReviewProcessData;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

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

        // 일반 코드 리뷰는 팀 규칙을 섞지 않고 코드 자체만 전달한다.
        AiReviewResponse generalResponse =
                aiReviewClient.review(
                        data.generalSystemPrompt(),
                        data.sourceCode(),
                        aiProperties.format()
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
                        aiProperties.format()
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
}
