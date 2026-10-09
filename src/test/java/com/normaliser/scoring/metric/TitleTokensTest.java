package com.normaliser.scoring.metric;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.Test;

class TitleTokensTest {

    @Test
    void of_shouldSplitOnSpaces() {
        assertEquals(Set.of("java", "engineer"), TitleTokens.of("java engineer"));
    }

    @Test
    void of_shouldReturnEmptySetForEmptyString() {
        assertEquals(Set.of(), TitleTokens.of(""));
    }

    @Test
    void of_shouldDropEmptyTokensFromRunsOfSpaces() {
        assertEquals(Set.of("chief", "accountant"), TitleTokens.of("chief  accountant"));
    }

    @Test
    void of_shouldRejectNull() {
        assertThrows(IllegalArgumentException.class, () -> TitleTokens.of(null));
    }

    @Test
    void of_shouldKeepHashInToken() {
        Set<String> tokens = TitleTokens.of("c# engineer");
        assertTrue(tokens.contains("c#"));
        assertTrue(tokens.contains("engineer"));
    }
}
