package com.normaliser.scoring.metric;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class CoverageTest {

    private final Coverage coverage = new Coverage();

    @Test
    void score_shouldReturnOneWhenSmallerTitleIsFullyCovered() {
        // smaller {accountant} fully inside {chief, accountant}
        assertEquals(1.0, coverage.score("chief accountant", "accountant"));
    }

    @Test
    void score_shouldReturnZeroForDisjointTitles() {
        assertEquals(0.0, coverage.score("java engineer", "quantity surveyor"));
    }

    @Test
    void score_shouldReturnPartialCoverage() {
        // {java, engineer} vs {software, engineer}: intersection 1, smaller size 2 → 0.5
        assertEquals(0.5, coverage.score("java engineer", "software engineer"), 1e-9);
    }

    @Test
    void score_shouldReturnZeroWhenEitherSideIsEmpty() {
        assertEquals(0.0, coverage.score("", "accountant"));
        assertEquals(0.0, coverage.score("accountant", ""));
    }

    @Test
    void score_shouldRejectNullInput() {
        assertThrows(IllegalArgumentException.class, () -> coverage.score(null, "accountant"));
    }

    @Test
    void score_shouldRejectNullCandidate() {
        assertThrows(IllegalArgumentException.class, () -> coverage.score("accountant", null));
    }
}
