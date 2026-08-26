package com.reviewer.github.webhook.model.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.reviewer.github.webhook.model.dto.GithubReviewCommentData;

import tools.jackson.databind.json.JsonMapper;

class GithubReviewCommentFormatterTest {

    private final GithubReviewCommentFormatter formatter =
            new GithubReviewCommentFormatter(JsonMapper.builder().build());

    @Test
    void formatsActionableFindingsAndSummary() {
        GithubReviewCommentData data = new GithubReviewCommentData(
                42L,
                "gpt-test",
                response(
                        "VIOLATION",
                        "Null 처리 누락",
                        "src/App.java:12",
                        "value를 바로 역참조합니다.",
                        "NPE가 발생할 수 있습니다.",
                        "null 검사를 추가하세요."
                ),
                response(
                        "PASS",
                        "팀 규칙 통과",
                        "",
                        "",
                        "규칙을 준수합니다.",
                        ""
                )
        );

        String comment = formatter.format(
                data,
                "0123456789abcdef"
        );

        assertTrue(comment.contains("reviewmate-ai-code-review:0123456789abcdef"));
        assertTrue(comment.contains("| `0123456` | `gpt-test` | 1 | 0 | 1 | 0 |"));
        assertTrue(comment.contains("[위반] Null 처리 누락"));
        assertTrue(comment.contains("ReviewMate review ID: `42`"));
        assertFalse(comment.contains("### [참고] 팀 규칙 통과"));
    }

    @Test
    void escapesAiGeneratedHtml() {
        GithubReviewCommentData data = new GithubReviewCommentData(
                1L,
                "model",
                response(
                        "VIOLATION",
                        "</details><script>alert(1)</script>",
                        "file`name",
                        "<b>evidence</b>",
                        "description",
                        "suggestion"
                ),
                "{\"reviews\":[]}"
        );

        String comment = formatter.format(data, "abcdef0");

        assertFalse(comment.contains("<script>"));
        assertTrue(comment.contains("&lt;script&gt;"));
        assertTrue(comment.contains("fileˋname"));
    }

    private String response(
            String status,
            String title,
            String location,
            String evidence,
            String description,
            String suggestion
    ) {
        return """
                {
                  "reviews": [
                    {
                      "status": "%s",
                      "title": "%s",
                      "location": "%s",
                      "evidence": "%s",
                      "description": "%s",
                      "suggestion": "%s"
                    }
                  ]
                }
                """.formatted(
                status,
                title,
                location,
                evidence,
                description,
                suggestion
        );
    }
}
