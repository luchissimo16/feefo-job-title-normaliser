package com.normaliser.scoring.metric;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ContainmentTest {

    private final Containment containment = new Containment();

    @Test
    void score_shouldReturnOneWhenOneTitleContainsTheOther() {
        assertEquals(1.0, containment.score("chief accountant", "accountant"));
        assertEquals(1.0, containment.score("accountant", "chief accountant"));
    }

    @Test
    void score_shouldReturnOneForIdenticalTitles() {
        assertEquals(1.0, containment.score("accountant", "accountant"));
    }

    @Test
    void score_shouldReturnZeroWhenNeitherContainsTheOther() {
        assertEquals(0.0, containment.score("java engineer", "software engineer"));
    }

    @Test
    void score_shouldRejectNullInput() {
        assertThrows(IllegalArgumentException.class, () -> containment.score(null, "accountant"));
    }

    @Test
    void score_shouldRejectNullCandidate() {
        assertThrows(IllegalArgumentException.class, () -> containment.score("accountant", null));
    }
}
