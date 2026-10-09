package com.normaliser.scoring.metric;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class JaccardTest {

    private final Jaccard jaccard = new Jaccard();

    @Test
    void score_shouldReturnOneForIdenticalTokenSets() {
        assertEquals(1.0, jaccard.score("software engineer", "software engineer"));
    }

    @Test
    void score_shouldReturnZeroForDisjointTitles() {
        assertEquals(0.0, jaccard.score("java engineer", "quantity surveyor"));
    }

    @Test
    void score_shouldReturnPartialOverlap() {
        // {java, engineer} ∩ {software, engineer} = 1; union = 3 → 1/3
        assertEquals(1.0 / 3.0, jaccard.score("java engineer", "software engineer"), 1e-9);
    }

    @Test
    void score_shouldReturnZeroWhenOneSideIsEmpty() {
        assertEquals(0.0, jaccard.score("", "engineer"));
        assertEquals(0.0, jaccard.score("engineer", ""));
    }

    @Test
    void score_shouldReturnOneWhenBothSidesAreEmpty() {
        assertEquals(1.0, jaccard.score("", ""));
    }

    @Test
    void score_shouldRejectNullInput() {
        assertThrows(IllegalArgumentException.class, () -> jaccard.score(null, "engineer"));
    }

    @Test
    void score_shouldRejectNullCandidate() {
        assertThrows(IllegalArgumentException.class, () -> jaccard.score("engineer", null));
    }
}
