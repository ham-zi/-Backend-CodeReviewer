package com.reviewer.github.webhook.model.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.reviewer.github.model.dto.GithubFileResponse;
import com.reviewer.github.webhook.model.dto.GithubFormattedReview;
import com.reviewer.github.webhook.model.dto.GithubInlineReviewComment;
import com.reviewer.github.webhook.model.dto.GithubReviewCommentData;
import com.reviewer.github.webhook.model.service.GithubDiffLineResolver.ResolvedLine;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@Component
public class GithubReviewCommentFormatter {

    private static final int MAX_SUMMARY_LENGTH = 60_000;
    private static final int MAX_TABLE_ITEMS = 5;
    private static final Pattern LOCATION_PATTERN = Pattern.compile(
            "^(.+?):(\\d+)(?:-\\d+)?(?:\\D.*)?$"
    );
    private static final String TRUNCATED_NOTICE =
            "\n\n---\n코멘트 길이 제한으로 일부 결과가 생략되었습니다.";

    private final JsonMapper jsonMapper;
    private final GithubDiffLineResolver lineResolver;

    public GithubReviewCommentFormatter(
            JsonMapper jsonMapper,
            GithubDiffLineResolver lineResolver
    ) {
        this.jsonMapper = jsonMapper;
        this.lineResolver = lineResolver;
    }

    public GithubFormattedReview format(
            GithubReviewCommentData data,
            String headSha,
            List<GithubFileResponse> files
    ) {
        List<Finding> all = new ArrayList<>();
        all.addAll(parseFindings(data.generalRawResponse(), "일반 코드 리뷰"));
        all.addAll(parseFindings(data.ruleRawResponse(), "팀 규칙 리뷰"));

        List<Finding> actionable = all.stream()
                .filter(Finding::isActionable)
                .toList();

        Map<String, Set<Integer>> changedLines =
                lineResolver.changedLines(files);
        List<GithubInlineReviewComment> inlineComments = new ArrayList<>();

        for (Finding finding : actionable) {
            lineResolver.resolve(
                    changedLines,
                    finding.filePath(),
                    finding.startLine()
            ).ifPresent(line -> inlineComments.add(toInlineComment(finding, line)));
        }

        return new GithubFormattedReview(
                formatSummary(
                        data,
                        headSha,
                        actionable,
                        inlineComments.size()
                ),
                List.copyOf(inlineComments)
        );
    }

    private String formatSummary(
            GithubReviewCommentData data,
            String headSha,
            List<Finding> findings,
            int inlineCommentCount
    ) {
        List<Finding> risks = byStatus(findings, "RISK");
        List<Finding> warnings = byStatus(findings, "WARNING");
        List<Finding> recommendations = byStatus(findings, "RECOMMENDATION");

        StringBuilder comment = new StringBuilder();
        comment.append("<!-- reviewmate-ai-code-review:")
               .append(escapeHtml(headSha))
               .append(" -->\n")
               .append("## ReviewMate AI 코드 리뷰 요약\n\n")
               .append("| 커밋 | 모델 | 위험 | 주의 | 권고 |\n")
               .append("|---|---|---:|---:|---:|\n")
               .append("| `")
               .append(shortSha(headSha))
               .append("` | `")
               .append(escapeInlineCode(data.aiModel()))
               .append("` | ")
               .append(risks.size())
               .append(" | ")
               .append(warnings.size())
               .append(" | ")
               .append(recommendations.size())
               .append(" |\n\n")
               .append("| 분류 | 항목 |\n")
               .append("|---|---|\n");

        appendSummaryRow(comment, "🚨 위험", risks);
        appendSummaryRow(comment, "⚠️ 주의", warnings);
        appendSummaryRow(comment, "💡 권고", recommendations);

        if (findings.isEmpty()) {
            comment.append("\n> 리뷰가 필요한 문제를 발견하지 못했습니다.\n");
        } else {
            int unresolved = findings.size() - inlineCommentCount;
            comment.append("\n> 문제 라인에 인라인 리뷰 **")
                   .append(inlineCommentCount)
                   .append("개**를 남겼습니다.");

            if (unresolved > 0) {
                comment.append(" diff에서 정확한 변경 라인을 확인하지 못한 **")
                       .append(unresolved)
                       .append("개**는 위 표에만 요약했습니다.");
            }
            comment.append("\n");
        }

        comment.append("\n---\n")
               .append("ReviewMate review ID: `")
               .append(data.reviewId())
               .append("`");

        return truncateSummary(comment.toString());
    }

    private void appendSummaryRow(
            StringBuilder comment,
            String label,
            List<Finding> findings
    ) {
        comment.append("| ")
               .append(label)
               .append(" (")
               .append(findings.size())
               .append(") | ");

        if (findings.isEmpty()) {
            comment.append("- |\n");
            return;
        }

        int visible = Math.min(findings.size(), MAX_TABLE_ITEMS);
        for (int index = 0; index < visible; index++) {
            if (index > 0) {
                comment.append("<br>");
            }

            Finding finding = findings.get(index);
            String location = displayLocation(finding);
            if (!location.isBlank()) {
                comment.append("`")
                       .append(escapeInlineCode(location))
                       .append("` ");
            }
            comment.append(escapeTable(truncate(finding.title(), 100)));
        }

        if (findings.size() > visible) {
            comment.append("<br>외 ")
                   .append(findings.size() - visible)
                   .append("개");
        }
        comment.append(" |\n");
    }

    private GithubInlineReviewComment toInlineComment(
            Finding finding,
            ResolvedLine line
    ) {
        boolean concise = !"RISK".equals(finding.status());
        int detailLimit = concise ? 220 : 1_000;

        StringBuilder body = new StringBuilder();
        body.append("**")
            .append(statusLabel(finding.status()))
            .append(" · ")
            .append(escapeHtml(truncate(finding.title(), 160)))
            .append("**");

        if (!finding.description().isBlank()) {
            body.append("\n\n")
                .append(escapeHtml(truncate(finding.description(), detailLimit)));
        }

        if (!finding.suggestion().isBlank()) {
            body.append("\n\n**제안:** ")
                .append(escapeHtml(truncate(finding.suggestion(), detailLimit)));
        }

        return new GithubInlineReviewComment(
                line.path(),
                line.line(),
                "RIGHT",
                body.toString()
        );
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
                String location = text(review, "location");
                ParsedLocation parsedLocation = parseLocation(location);
                String filePath = text(review, "filePath");
                int startLine = integer(review, "startLine");

                if (filePath.isBlank()) {
                    filePath = parsedLocation.filePath();
                }
                if (startLine <= 0) {
                    startLine = parsedLocation.startLine();
                }

                findings.add(new Finding(
                        normalizeStatus(text(review, "status")),
                        text(review, "title"),
                        location,
                        filePath,
                        startLine,
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

    private ParsedLocation parseLocation(String location) {
        Matcher matcher = LOCATION_PATTERN.matcher(location.trim());
        if (!matcher.matches()) {
            return new ParsedLocation("", 0);
        }

        return new ParsedLocation(
                matcher.group(1).trim(),
                Integer.parseInt(matcher.group(2))
        );
    }

    private String normalizeStatus(String status) {
        return switch (status) {
            case "VIOLATION" -> "RISK";
            case "INSUFFICIENT_CONTEXT" -> "WARNING";
            default -> status;
        };
    }

    private List<Finding> byStatus(
            List<Finding> findings,
            String status
    ) {
        return findings.stream()
                .filter(finding -> status.equals(finding.status()))
                .toList();
    }

    private String text(JsonNode node, String fieldName) {
        JsonNode value = node == null ? null : node.get(fieldName);
        return value == null ? "" : value.asText("");
    }

    private int integer(JsonNode node, String fieldName) {
        JsonNode value = node == null ? null : node.get(fieldName);
        return value == null ? 0 : value.asInt(0);
    }

    private String statusLabel(String status) {
        return switch (status) {
            case "RISK" -> "🚨 위험";
            case "WARNING" -> "⚠️ 주의";
            case "RECOMMENDATION" -> "💡 권고";
            default -> "참고";
        };
    }

    private String displayLocation(Finding finding) {
        if (!finding.filePath().isBlank() && finding.startLine() > 0) {
            return finding.filePath() + ":" + finding.startLine();
        }
        return finding.location();
    }

    private String shortSha(String headSha) {
        if (headSha == null) {
            return "unknown";
        }
        return headSha.substring(0, Math.min(7, headSha.length()));
    }

    private String escapeTable(String value) {
        return escapeHtml(value).replace("|", "\\|");
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

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value == null ? "" : value;
        }
        return value.substring(0, Math.max(0, maxLength - 1)) + "…";
    }

    private String truncateSummary(String summary) {
        if (summary.length() <= MAX_SUMMARY_LENGTH) {
            return summary;
        }
        int keep = MAX_SUMMARY_LENGTH - TRUNCATED_NOTICE.length();
        return summary.substring(0, keep) + TRUNCATED_NOTICE;
    }

    private record ParsedLocation(String filePath, int startLine) {
    }

    private record Finding(
            String status,
            String title,
            String location,
            String filePath,
            int startLine,
            String description,
            String suggestion
    ) {
        private boolean isActionable() {
            return "RISK".equals(status) ||
                   "WARNING".equals(status) ||
                   "RECOMMENDATION".equals(status);
        }
    }
}
