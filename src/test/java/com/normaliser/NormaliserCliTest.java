package com.normaliser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * CLI passes args straight to {@link Normaliser}; invalid titles surface Normaliser exceptions.
 * (No-args usage is a {@code main} concern exercised manually / via the process exit path.)
 */
class NormaliserCliTest {

    @Test
    void normaliser_shouldRejectBlankTitleSameAsCliWouldPassThrough() {
        Normaliser normaliser = new Normaliser();

        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> normaliser.normalise(""));

        assertEquals("jobTitle must not be blank", ex.getMessage());
    }

    @Test
    void normaliser_shouldRejectPunctuationOnlyTitle() {
        Normaliser normaliser = new Normaliser();

        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> normaliser.normalise("----"));

        assertEquals("jobTitle must contain at least one meaningful character", ex.getMessage());
    }
}
