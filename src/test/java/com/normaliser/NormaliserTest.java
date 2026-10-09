package com.normaliser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.normaliser.scoring.DefaultScorer;
import com.normaliser.scoring.Scorer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

/** Core Normaliser behaviour: matching, validation, and construction. */
class NormaliserTest {

    private static final List<String> ASSESSMENT_TITLES =
            List.of(
                    "Architect",
                    "Software engineer",
                    "Quantity surveyor",
                    "Accountant",
                    "Product manager",
                    "Data scientist",
                    "Business analyst");

    private Normaliser normaliser;

    @BeforeEach
    void setUp() {
        normaliser = new Normaliser(ASSESSMENT_TITLES);
    }

    // Assessment sample cases

    @Test
    void noArgConstructor_shouldMatchSampleApi() {
        Normaliser n = new Normaliser();

        assertEquals("Software engineer", n.normalise("Java engineer"));
        assertEquals("Software engineer", n.normalise("C# engineer"));
        assertEquals("Accountant", n.normalise("Accountant"));
        assertEquals("Accountant", n.normalise("Chief Accountant"));
    }

    @ParameterizedTest(name = "{0} -> {1}")
    @CsvSource({
        "Java engineer, Software engineer",
        "C# engineer, Software engineer",
        "Accountant, Accountant",
        "Chief Accountant, Accountant",
        "Senior product manager, Product manager",
        "Lead data scientist, Data scientist",
        "Junior business analyst, Business analyst",
        "Quantity surveyor, Quantity surveyor",
        "Architect, Architect",
        "Product manager, Product manager",
        "Data scientist, Data scientist",
        "Business analyst, Business analyst"
    })
    void normalise_shouldMatchExpectedCanonicalTitle(String input, String expected) {
        assertEquals(expected, normaliser.normalise(input));
    }

    // Preprocessing and validation edge cases (parameterised)

    @ParameterizedTest(name = "[{0}] -> {1}")
    @CsvSource({
        "' JAVA ENGINEER ', Software engineer",
        "'  Java engineer  ', Software engineer",
        "'Chief   Accountant', Accountant",
        "java engineer, Software engineer",
        "CHIEF ACCOUNTANT, Accountant"
    })
    void normalise_shouldHandleMixedCaseAndWhitespace(String input, String expected) {
        assertEquals(expected, normaliser.normalise(input));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    void normalise_shouldRejectNullOrBlankInput(String input) {
        assertThrows(IllegalArgumentException.class, () -> normaliser.normalise(input));
    }

    @ParameterizedTest
    @ValueSource(strings = {"----", "...", "!!!", "--- ---"})
    void normalise_shouldRejectPunctuationOnlyInput(String input) {
        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> normaliser.normalise(input));
        assertTrue(ex.getMessage().contains("meaningful"));
    }

    @Test
    void normalise_shouldUseCustomCanonicalTitles() {
        Normaliser custom = new Normaliser(List.of("UX designer", "Legal counsel"));

        assertEquals("UX designer", custom.normalise("Senior UX designer"));
    }

    @Test
    void normalise_shouldRejectWhenBelowMinimumScore() {
        Normaliser strict = new Normaliser(ASSESSMENT_TITLES, new DefaultScorer(), 0.95);

        NoSuitableMatchException ex =
                assertThrows(NoSuitableMatchException.class, () -> strict.normalise("Chef"));

        assertEquals("Chef", ex.input());
        assertEquals(0.95, ex.minimumScore());
        assertTrue(ex.bestScore() < 0.95);
        assertTrue(ex.bestTitle() != null && !ex.bestTitle().isBlank());
    }

    @Test
    void normalise_shouldReturnBestMatchWhenMinimumScoreIsZero() {
        Normaliser permissive = new Normaliser(ASSESSMENT_TITLES, new DefaultScorer(), 0.0);

        NormalisationResult result = permissive.normaliseWithScore("Chef");

        assertTrue(result.title() != null && !result.title().isBlank());
        assertTrue(result.score() >= 0.0);
    }

    @Test
    void normaliseWithScore_shouldExposeScoreForExactMatch() {
        NormalisationResult result = normaliser.normaliseWithScore("Accountant");

        assertEquals("Accountant", result.title());
        assertEquals(1.0, result.score());
    }

    // Tiebreak and selection via stub Scorer (programmed to interface)

    @Test
    void normalise_shouldKeepFirstTitleWhenScoresAreTied() {
        Scorer stub =
                (input, candidate) ->
                        switch (candidate) {
                            case "alpha", "beta" -> 0.8;
                            default -> 0.0;
                        };

        Normaliser custom = new Normaliser(List.of("Alpha", "Beta"), stub);

        assertEquals("Alpha", custom.normalise("anything"));
    }

    @Test
    void normalise_shouldReplaceWhenLaterCandidateScoresHigher() {
        Scorer stub =
                (input, candidate) ->
                        switch (candidate) {
                            case "alpha" -> 0.5;
                            case "beta" -> 0.9;
                            default -> 0.0;
                        };

        Normaliser custom = new Normaliser(List.of("Alpha", "Beta"), stub);

        assertEquals("Beta", custom.normalise("anything"));
    }

    @Test
    void normalise_shouldRejectWhenStubScoreIsBelowMinimum() {
        Scorer stub = (input, candidate) -> 0.4;
        Normaliser custom = new Normaliser(List.of("Alpha", "Beta"), stub, 0.5);

        NoSuitableMatchException ex =
                assertThrows(NoSuitableMatchException.class, () -> custom.normalise("weak"));

        assertEquals(0.4, ex.bestScore());
        assertEquals(0.5, ex.minimumScore());
    }

    // Construction and NormalisationResult

    @Test
    void constructor_shouldRejectNullCanonicalTitles() {
        assertThrows(NullPointerException.class, () -> new Normaliser(null));
    }

    @Test
    void constructor_shouldRejectEmptyCanonicalTitles() {
        assertThrows(IllegalArgumentException.class, () -> new Normaliser(List.of()));
    }

    @Test
    void constructor_shouldRejectBlankCanonicalTitle() {
        assertThrows(
                IllegalArgumentException.class, () -> new Normaliser(List.of("Architect", "  ")));
    }

    @Test
    void constructor_shouldRejectNullScorer() {
        assertThrows(NullPointerException.class, () -> new Normaliser(List.of("Architect"), null));
    }

    @Test
    void constructor_shouldRejectInvalidMinimumScore() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Normaliser(List.of("Architect"), new DefaultScorer(), 1.5));
    }

    @Test
    void constructor_shouldDefensivelyCopyCanonicalTitles() {
        var titles = new java.util.ArrayList<>(List.of("Architect", "Accountant"));
        Normaliser custom = new Normaliser(titles);

        titles.clear();

        assertEquals("Architect", custom.normalise("Architect"));
    }

    @Test
    void matchResult_shouldRejectInvalidScore() {
        assertThrows(IllegalArgumentException.class, () -> new NormalisationResult("Architect", 1.1));
    }

    @Test
    void matchResult_shouldRejectNullOrBlankTitle() {
        assertThrows(IllegalArgumentException.class, () -> new NormalisationResult(null, 0.5));
        assertThrows(IllegalArgumentException.class, () -> new NormalisationResult("  ", 0.5));
    }
}
