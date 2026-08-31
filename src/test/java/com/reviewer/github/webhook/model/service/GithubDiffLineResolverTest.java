package com.reviewer.github.webhook.model.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.reviewer.github.model.dto.GithubFileResponse;

class GithubDiffLineResolverTest {

    private final GithubDiffLineResolver resolver =
            new GithubDiffLineResolver();

    @Test
    void parsesAddedRightSideLinesFromMultipleHunks() {
        GithubFileResponse file = new GithubFileResponse(
                "src/App.java",
                "modified",
                3,
                1,
                """
                        @@ -10,3 +10,4 @@
                         context
                        -removed
                        +added
                        +another
                         context
                        @@ -30 +31,2 @@
                        +later
                         context
                        """
        );

        Map<String, Set<Integer>> lines =
                resolver.changedLines(List.of(file));

        assertEquals(Set.of(11, 12, 31), lines.get("src/App.java"));
    }

    @Test
    void resolvesStartLineOrAtMostTwoLinesForward() {
        Map<String, Set<Integer>> lines = Map.of(
                "src/App.java",
                Set.of(12)
        );

        assertEquals(
                12,
                resolver.resolve(lines, "b/src/App.java", 10).orElseThrow().line()
        );
        assertTrue(resolver.resolve(lines, "src/App.java", 9).isEmpty());
        assertTrue(resolver.resolve(lines, "src/App.java", 13).isEmpty());
    }
}
