package com.normaliser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.normaliser.scoring.DefaultScorer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

    @Test
    void normalise_shouldReturnSoftwareEngineer_whenInputIsJavaEngineer() {
        assertEquals("Software engineer", normaliser.normalise("Java engineer"));
    }

    @Test
    void normalise_shouldReturnSoftwareEngineer_whenInputIsCsharpEngineer() {
        assertEquals("Software engineer", normaliser.normalise("C# engineer"));
    }

    @Test
    void normalise_shouldReturnAccountant_whenInputIsAccountant() {
        assertEquals("Accountant", normaliser.normalise("Accountant"));
    }

    @Test
    void normalise_shouldReturnAccountant_whenInputIsChiefAccountant() {
        assertEquals("Accountant", normaliser.normalise("Chief Accountant"));
    }

    // Preprocessing (case / whitespace)

    @Test
    void normalise_shouldIgnoreCase() {
        assertEquals("Software engineer", normaliser.normalise(" JAVA ENGINEER "));
    }

    @Test
    void normalise_shouldIgnoreSurroundingWhitespace() {
        assertEquals("Software engineer", normaliser.normalise("  Java engineer  "));
    }

    @Test
    void normalise_shouldCollapseInternalWhitespace() {
        assertEquals("Accountant", normaliser.normalise("Chief   Accountant"));
    }

    @Test
    void normalise_shouldReturnExactCanonicalForm_whenExactMatch() {
        assertEquals("Quantity surveyor", normaliser.normalise("Quantity surveyor"));
        assertEquals("Architect", normaliser.normalise("Architect"));
        assertEquals("Product manager", normaliser.normalise("Product manager"));
        assertEquals("Data scientist", normaliser.normalise("Data scientist"));
        assertEquals("Business analyst", normaliser.normalise("Business analyst"));
    }

    @Test
    void normalise_shouldReturnProductManager_whenInputIsSeniorProductManager() {
        assertEquals("Product manager", normaliser.normalise("Senior product manager"));
    }

    @Test
    void normalise_shouldReturnDataScientist_whenInputIsLeadDataScientist() {
        assertEquals("Data scientist", normaliser.normalise("Lead data scientist"));
    }

    @Test
    void normalise_shouldReturnBusinessAnalyst_whenInputIsJuniorBusinessAnalyst() {
        assertEquals("Business analyst", normaliser.normalise("Junior business analyst"));
    }

    // Input validation and quality threshold

    @Test
    void normalise_shouldRejectNullInput() {
        assertThrows(IllegalArgumentException.class, () -> normaliser.normalise(null));
    }

    @Test
    void normalise_shouldRejectBlankInput() {
        assertThrows(IllegalArgumentException.class, () -> normaliser.normalise("   "));
    }

    @Test
    void normalise_shouldRejectEmptyInput() {
        assertThrows(IllegalArgumentException.class, () -> normaliser.normalise(""));
    }

    @Test
    void normalise_shouldUseCustomCanonicalTitles() {
        Normaliser custom = new Normaliser(List.of("UX designer", "Legal counsel"));

        assertEquals("UX designer", custom.normalise("Senior UX designer"));
    }

    @Test
    void normalise_shouldRejectWhenBelowMinimumQuality() {
        Normaliser strict = new Normaliser(ASSESSMENT_TITLES, new DefaultScorer(), 0.95);

        NoSuitableMatchException ex =
                assertThrows(NoSuitableMatchException.class, () -> strict.normalise("Chef"));

        assertEquals("Chef", ex.input());
        assertEquals(0.95, ex.minimumQuality());
        assertTrue(ex.bestQuality() < 0.95);
        assertTrue(ex.bestTitle() != null && !ex.bestTitle().isBlank());
    }

    @Test
    void normaliseWithQuality_shouldExposeScoreForExactMatch() {
        NormalisationResult result = normaliser.normaliseWithQuality("Accountant");

        assertEquals("Accountant", result.title());
        assertEquals(1.0, result.quality());
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
    void constructor_shouldRejectInvalidMinimumQuality() {
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
    void matchResult_shouldRejectInvalidQuality() {
        assertThrows(IllegalArgumentException.class, () -> new NormalisationResult("Architect", 1.1));
    }

    @Test
    void normalise_shouldPreferHighestScoreDeterministically() {
        NormalisationResult javaEngineer = normaliser.normaliseWithQuality("Java engineer");
        NormalisationResult accountant = normaliser.normaliseWithQuality("Java engineer");

        assertEquals(javaEngineer, accountant);
        assertTrue(javaEngineer.quality() > 0.0);
        assertEquals("Software engineer", javaEngineer.title());
    }
}
