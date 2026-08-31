package com.reviewer.github.webhook.model.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.reviewer.github.model.dto.GithubFileResponse;
import com.reviewer.github.webhook.model.dto.GithubFormattedReview;
import com.reviewer.github.webhook.model.dto.GithubReviewCommentData;

import tools.jackson.databind.json.JsonMapper;

class GithubReviewCommentFormatterTest {

    private final GithubReviewCommentFormatter formatter =
            new GithubReviewCommentFormatter(
                    JsonMapper.builder().build(),
                    new GithubDiffLineResolver()
            );

    @Test
    void formatsInlineCommentsAndCompactSummaryBySeverity() {
        GithubReviewCommentData data = new GithubReviewCommentData(
                42L,
                "gpt-test",
                response(
                        "RISK",
                        "Null 처리 누락",
                        "src/App.java:12",
                        "src/App.java",
                        12,
                        "value를 바로 역참조해 NPE가 발생할 수 있습니다.",
                        "null 검사를 추가하세요."
                ),
                response(
                        "RECOMMENDATION",
                        "이름을 명확하게 지정",
                        "src/App.java:14",
                        "src/App.java",
                        14,
                        "변수의 역할을 이름에서 알기 어렵습니다.",
                        "의도를 드러내는 이름을 사용하세요."
                )
        );

        GithubFormattedReview result = formatter.format(
                data,
                "0123456789abcdef",
                List.of(file("src/App.java"))
        );

        assertEquals(2, result.inlineComments().size());
        assertEquals(12, result.inlineComments().get(0).line());
        assertEquals("RIGHT", result.inlineComments().get(0).side());
        assertTrue(result.inlineComments().get(0).body().contains("위험"));
        assertTrue(result.summary().contains(
                "reviewmate-ai-code-review:0123456789abcdef"
        ));
        assertTrue(result.summary().contains(
                "| `0123456` | `gpt-test` | 1 | 0 | 1 |"
        ));
        assertTrue(result.summary().contains("| 🚨 위험 (1) |"));
        assertTrue(result.summary().contains("ReviewMate review ID: `42`"));
        assertFalse(result.summary().contains("value를 바로 역참조"));
    }

    @Test
    void usesNextTwoChangedLinesAndSupportsLegacyResponse() {
        GithubReviewCommentData data = new GithubReviewCommentData(
                1L,
                "model",
                legacyResponse(
                        "VIOLATION",
                        "기존 위험 항목",
                        "src/App.java:11",
                        "설명",
                        "제안"
                ),
                "{\"reviews\":[]}"
        );

        GithubFormattedReview result = formatter.format(
                data,
                "abcdef0",
                List.of(file("src/App.java"))
        );

        assertEquals(1, result.inlineComments().size());
        assertEquals(12, result.inlineComments().get(0).line());
        assertTrue(result.summary().contains("위험 (1)"));
    }

    @Test
    void escapesAiGeneratedHtml() {
        GithubReviewCommentData data = new GithubReviewCommentData(
                1L,
                "model",
                response(
                        "WARNING",
                        "</details><script>alert(1)</script>",
                        "src/App.java:12",
                        "src/App.java",
                        12,
                        "<b>description</b>",
                        "suggestion"
                ),
                "{\"reviews\":[]}"
        );

        GithubFormattedReview result = formatter.format(
                data,
                "abcdef0",
                List.of(file("src/App.java"))
        );

        assertFalse(result.summary().contains("<script>"));
        assertTrue(result.summary().contains("&lt;script&gt;"));
        assertTrue(result.inlineComments().get(0).body().contains(
                "&lt;b&gt;description&lt;/b&gt;"
        ));
    }

    private GithubFileResponse file(String filename) {
        return new GithubFileResponse(
                filename,
                "modified",
                2,
                0,
                """
                        @@ -10,3 +10,5 @@ class App {
                         unchanged
                         another unchanged
                        +added at twelve
                         unchanged again
                        +added at fourteen
                        """
        );
    }

    private String response(
            String status,
            String title,
            String location,
            String filePath,
            int startLine,
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
                      "filePath": "%s",
                      "startLine": %d,
                      "evidence": "evidence",
                      "description": "%s",
                      "suggestion": "%s"
                    }
                  ]
                }
                """.formatted(
                status,
                title,
                location,
                filePath,
                startLine,
                description,
                suggestion
        );
    }

    private String legacyResponse(
            String status,
            String title,
            String location,
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
                      "evidence": "",
                      "description": "%s",
                      "suggestion": "%s"
                    }
                  ]
                }
                """.formatted(status, title, location, description, suggestion);
    }
}
