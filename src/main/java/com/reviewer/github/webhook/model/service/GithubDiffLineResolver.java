package com.reviewer.github.webhook.model.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.reviewer.github.model.dto.GithubFileResponse;

@Component
public class GithubDiffLineResolver {

    private static final Pattern HUNK_HEADER = Pattern.compile(
            "^@@ -\\d+(?:,\\d+)? \\+(\\d+)(?:,\\d+)? @@"
    );
    private static final int MAX_FORWARD_OFFSET = 2;

    public Map<String, Set<Integer>> changedLines(
            List<GithubFileResponse> files
    ) {
        Map<String, Set<Integer>> changedLines = new HashMap<>();

        for (GithubFileResponse file : files) {
            changedLines.put(
                    normalizePath(file.filename()),
                    parseAddedLines(file.patch())
            );
        }

        return changedLines;
    }

    public Optional<ResolvedLine> resolve(
            Map<String, Set<Integer>> changedLines,
            String filePath,
            int startLine
    ) {
        if (startLine <= 0) {
            return Optional.empty();
        }

        String normalizedPath = normalizePath(filePath);
        Set<Integer> validLines = changedLines.get(normalizedPath);

        if (validLines == null || validLines.isEmpty()) {
            return Optional.empty();
        }

        for (int offset = 0; offset <= MAX_FORWARD_OFFSET; offset++) {
            int candidate = startLine + offset;
            if (validLines.contains(candidate)) {
                return Optional.of(new ResolvedLine(normalizedPath, candidate));
            }
        }

        return Optional.empty();
    }

    private Set<Integer> parseAddedLines(String patch) {
        Set<Integer> addedLines = new HashSet<>();

        if (patch == null || patch.isBlank()) {
            return addedLines;
        }

        int newLine = -1;
        for (String line : patch.split("\\R", -1)) {
            Matcher matcher = HUNK_HEADER.matcher(line);
            if (matcher.find()) {
                newLine = Integer.parseInt(matcher.group(1));
                continue;
            }

            if (newLine < 0 || line.isEmpty()) {
                continue;
            }

            char prefix = line.charAt(0);
            if (prefix == '+') {
                addedLines.add(newLine);
                newLine++;
            } else if (prefix == ' ') {
                newLine++;
            } else if (prefix != '-' && prefix != '\\') {
                newLine++;
            }
        }

        return addedLines;
    }

    private String normalizePath(String path) {
        if (path == null) {
            return "";
        }

        String normalized = path.trim()
                .replace("`", "")
                .replace('\\', '/');

        if (normalized.startsWith("a/") || normalized.startsWith("b/")) {
            return normalized.substring(2);
        }

        return normalized;
    }

    public record ResolvedLine(String path, int line) {
    }
}
