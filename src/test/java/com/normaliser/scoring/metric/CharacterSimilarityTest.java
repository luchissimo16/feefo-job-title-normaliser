package com.normaliser.scoring.metric;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CharacterSimilarityTest {

    private final CharacterSimilarity similarity = new CharacterSimilarity();

    @Test
    void score_shouldReturnOneForIdenticalStrings() {
        assertEquals(1.0, similarity.score("engineer", "engineer"));
    }

    @Test
    void score_shouldReturnOneForEmptyStrings() {
        assertEquals(1.0, similarity.score("", ""));
    }

    @Test
    void score_shouldBeLowerForDistantStrings() {
        double close = similarity.score("engineer", "engineers");
        double distant = similarity.score("engineer", "accountant");

        assertTrue(close > distant, "expected close " + close + " > distant " + distant);
    }

    @Test
    void score_shouldReflectSingleCharacterEdit() {
        // "cat" → "bat": 1 edit / 3 chars → 2/3
        assertEquals(2.0 / 3.0, similarity.score("cat", "bat"), 1e-9);
    }

    @Test
    void score_shouldRejectNullInput() {
        assertThrows(IllegalArgumentException.class, () -> similarity.score(null, "engineer"));
    }

    @Test
    void score_shouldRejectNullCandidate() {
        assertThrows(IllegalArgumentException.class, () -> similarity.score("engineer", null));
    }

    @Test
    void levenshteinDistance_shouldBeZeroForIdenticalStrings() {
        assertEquals(0, CharacterSimilarity.levenshteinDistance("abc", "abc"));
    }

    @Test
    void levenshteinDistance_shouldCountSingleEdit() {
        assertEquals(1, CharacterSimilarity.levenshteinDistance("cat", "bat"));
    }

    @Test
    void levenshteinDistance_shouldRejectNull() {
        assertThrows(
                IllegalArgumentException.class,
                () -> CharacterSimilarity.levenshteinDistance(null, "a"));
        assertThrows(
                IllegalArgumentException.class,
                () -> CharacterSimilarity.levenshteinDistance("a", null));
    }
}
