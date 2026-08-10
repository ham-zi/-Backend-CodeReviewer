package com.reviewer.review.model.service;


import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.reviewer.github.model.dto.GithubCompareResponse;
import com.reviewer.github.model.dto.GithubFileResponse;
import com.reviewer.ollama.client.OllamaClient;
import com.reviewer.review.model.dto.ReviewProcessData;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewAsyncService {

    private final OllamaClient ollamaClient;
    private final JsonMapper jsonMapper;
    private final ReviewTransactionService reviewTransactionService;

    @Async
    public void process(Long reviewId, GithubCompareResponse response) {

        try {
            // 짧은 트랜잭션
            // PROCESSING 변경 + LAZY 데이터 추출
            ReviewProcessData data =
                    reviewTransactionService.start(reviewId);

            StringBuilder sb = new StringBuilder();

            sb.append("##리뷰 대상 코드##\n");

            for (GithubFileResponse file : response.files()) {
                sb.append("파일: ")
                  .append(file.filename())
                  .append("\n");

                sb.append("변경사항:\n")
                  .append(file.patch())
                  .append("\n\n");
            }

            sb.append("##팀 규칙##\n");
            sb.append(data.ruleContent());

            // 여기는 트랜잭션 없음
            // Ollama가 오래 걸려도 DB 트랜잭션을 잡고 있지 않음
            String rawResponse =
                    ollamaClient.generate(sb.toString());
            log.info("RAW RESPONSE = {}", rawResponse);
            JsonNode json =
                    jsonMapper.readTree(rawResponse);
            log.info("PARSED JSON = {}", json);
            // 짧은 트랜잭션
            // ReviewItem 저장 + COMPLETED 변경
            reviewTransactionService.complete(
                    reviewId,
                    rawResponse,
                    json
            );

            log.info("비동기 코드 리뷰 완료. reviewId={}", reviewId);

        } catch (Exception e) {

            log.error(
                    "비동기 코드 리뷰 처리 중 예외 발생. reviewId={}",
                    reviewId,
                    e
            );

            try {
                reviewTransactionService.fail(reviewId);
            } catch (Exception failException) {
                log.error(
                        "리뷰 FAILED 상태 변경 중 예외 발생. reviewId={}",
                        reviewId,
                        failException
                );
            }
        }
    }
}