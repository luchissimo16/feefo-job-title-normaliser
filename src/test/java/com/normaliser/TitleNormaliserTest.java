package com.normaliser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Comparison-form preparation: case, whitespace, and punctuation. */
class TitleNormaliserTest {

    private TitleNormaliser titleNormaliser;

    @BeforeEach
    void setUp() {
        titleNormaliser = new TitleNormaliser();
    }

    @Test
    void prepare_shouldLowercaseAndTrim() {
        assertEquals("software engineer", titleNormaliser.prepare(" SOFTWARE ENGINEER "));
    }

    @Test
    void prepare_shouldCollapseInternalWhitespace() {
        assertEquals("chief accountant", titleNormaliser.prepare("Chief   Accountant"));
    }

    @Test
    void prepare_shouldPreserveHashInTokens() {
        assertEquals("c# engineer", titleNormaliser.prepare("C# engineer"));
    }

    @Test
    void prepare_shouldReplaceOtherPunctuationWithSpaces() {
        assertEquals("senior software engineer", titleNormaliser.prepare("Senior, Software-Engineer"));
    }

    @Test
    void prepare_shouldRejectNull() {
        assertThrows(IllegalArgumentException.class, () -> titleNormaliser.prepare(null));
    }
}
