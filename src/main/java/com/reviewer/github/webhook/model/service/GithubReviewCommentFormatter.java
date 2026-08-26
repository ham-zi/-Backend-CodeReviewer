package com.reviewer.github.webhook.model.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.reviewer.github.webhook.model.dto.GithubReviewCommentData;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@Component
public class GithubReviewCommentFormatter {

    private static final int MAX_COMMENT_LENGTH = 60_000;
    private static final String TRUNCATED_NOTICE =
            "\n\n---\n코멘트 길이 제한으로 일부 결과가 생략되었습니다.";

    private final JsonMapper jsonMapper;

    public GithubReviewCommentFormatter(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    public String format(
            GithubReviewCommentData data,
            String headSha
    ) {
        List<Finding> general = parseFindings(
                data.generalRawResponse(),
                "일반 코드 리뷰"
        );
        List<Finding> rules = parseFindings(
                data.ruleRawResponse(),
                "팀 규칙 리뷰"
        );

        List<Finding> all = new ArrayList<>(general);
        all.addAll(rules);

        long violations = count(all, "VIOLATION");
        long insufficientContext = count(all, "INSUFFICIENT_CONTEXT");
        long passed = count(all, "PASS");
        long notApplicable = count(all, "NOT_APPLICABLE");

        StringBuilder comment = new StringBuilder();
        comment.append("<!-- reviewmate-ai-code-review:")
               .append(escapeHtml(headSha))
               .append(" -->\n")
               .append("## ReviewMate AI 코드 리뷰\n\n")
               .append("| 커밋 | 모델 | 위반 | 추가 확인 필요 | 통과 | 해당 없음 |\n")
               .append("|---|---|---:|---:|---:|---:|\n")
               .append("| `")
               .append(shortSha(headSha))
               .append("` | `")
               .append(escapeInlineCode(data.aiModel()))
               .append("` | ")
               .append(violations)
               .append(" | ")
               .append(insufficientContext)
               .append(" | ")
               .append(passed)
               .append(" | ")
               .append(notApplicable)
               .append(" |\n\n");

        if (violations == 0 && insufficientContext == 0) {
            comment.append("> 리뷰가 필요한 문제를 발견하지 못했습니다.\n\n");
        } else {
            comment.append("> 수정 권장 항목 **")
                   .append(violations)
                   .append("개**, 추가 확인이 필요한 항목 **")
                   .append(insufficientContext)
                   .append("개**입니다.\n\n");
        }

        appendSection(comment, "일반 코드 리뷰", general, true);
        appendSection(comment, "팀 규칙 리뷰", rules, false);

        comment.append("\n---\n")
               .append("ReviewMate review ID: `")
               .append(data.reviewId())
               .append("`");

        return truncate(comment.toString());
    }

    private void appendSection(
            StringBuilder comment,
            String title,
            List<Finding> findings,
            boolean open
    ) {
        List<Finding> actionable = findings.stream()
                .filter(Finding::isActionable)
                .toList();

        comment.append("<details")
               .append(open ? " open" : "")
               .append(">\n<summary><strong>")
               .append(title)
               .append("</strong> — 조치 항목 ")
               .append(actionable.size())
               .append("개</summary>\n\n");

        if (actionable.isEmpty()) {
            comment.append("조치가 필요한 항목이 없습니다.\n\n");
        }

        for (Finding finding : actionable) {
            comment.append("### ")
                   .append(statusLabel(finding.status()))
                   .append(" ")
                   .append(escapeHtml(finding.title()))
                   .append("\n\n");

            if (!finding.location().isBlank()) {
                comment.append("- 위치: `")
                       .append(escapeInlineCode(finding.location()))
                       .append("`\n");
            }

            if (!finding.description().isBlank()) {
                comment.append("- 설명: ")
                       .append(escapeHtml(finding.description()))
                       .append("\n");
            }

            if (!finding.suggestion().isBlank()) {
                comment.append("- 제안: ")
                       .append(escapeHtml(finding.suggestion()))
                       .append("\n");
            }

            if (!finding.evidence().isBlank()) {
                comment.append("\n**근거**\n\n")
                       .append(blockQuote(finding.evidence()))
                       .append("\n");
            }

            comment.append("\n");
        }

        comment.append("</details>\n\n");
    }

    private List<Finding> parseFindings(String rawResponse, String reviewName) {
        try {
            JsonNode root = jsonMapper.readTree(rawResponse);
            JsonNode reviews = root == null ? null : root.get("reviews");

            if (reviews == null || !reviews.isArray()) {
                throw new IllegalStateException(
                        reviewName + " 응답에 reviews 배열이 없습니다."
                );
            }

            List<Finding> findings = new ArrayList<>();
            for (JsonNode review : reviews) {
                findings.add(new Finding(
                        text(review, "status"),
                        text(review, "title"),
                        text(review, "location"),
                        text(review, "evidence"),
                        text(review, "description"),
                        text(review, "suggestion")
                ));
            }
            return findings;
        } catch (Exception e) {
            throw new IllegalStateException(
                    reviewName + " 결과를 GitHub 코멘트로 변환할 수 없습니다.",
                    e
            );
        }
    }

    private String text(JsonNode node, String fieldName) {
        JsonNode value = node == null ? null : node.get(fieldName);
        return value == null ? "" : value.asText("");
    }

    private long count(List<Finding> findings, String status) {
        return findings.stream()
                .filter(finding -> status.equals(finding.status()))
                .count();
    }

    private String statusLabel(String status) {
        return switch (status) {
            case "VIOLATION" -> "[위반]";
            case "INSUFFICIENT_CONTEXT" -> "[추가 확인 필요]";
            default -> "[참고]";
        };
    }

    private String blockQuote(String value) {
        return escapeHtml(value).lines()
                .map(line -> "> " + line)
                .reduce((left, right) -> left + "\n" + right)
                .orElse("> ");
    }

    private String shortSha(String headSha) {
        if (headSha == null) {
            return "unknown";
        }
        return headSha.substring(0, Math.min(7, headSha.length()));
    }

    private String escapeInlineCode(String value) {
        return escapeHtml(value == null ? "" : value)
                .replace("`", "ˋ")
                .replace("\r", " ")
                .replace("\n", " ");
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private String truncate(String comment) {
        if (comment.length() <= MAX_COMMENT_LENGTH) {
            return comment;
        }
        int keep = MAX_COMMENT_LENGTH - TRUNCATED_NOTICE.length();
        return comment.substring(0, keep) + TRUNCATED_NOTICE;
    }

    private record Finding(
            String status,
            String title,
            String location,
            String evidence,
            String description,
            String suggestion
    ) {
        private boolean isActionable() {
            return "VIOLATION".equals(status) ||
                   "INSUFFICIENT_CONTEXT".equals(status);
        }
    }
}
