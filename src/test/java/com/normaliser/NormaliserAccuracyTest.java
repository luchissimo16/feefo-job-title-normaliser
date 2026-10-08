package com.normaliser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.util.List;

/** Checks that known inputs map to the expected titles in {@code canonical-titles.txt}. */
class NormaliserAccuracyTest {

    private static Normaliser normaliser;
    private static List<String> canonicalTitles;

    @BeforeAll
    static void setUp() throws IOException {
        canonicalTitles = CanonicalTitleRepository.load();
        normaliser = new Normaliser(canonicalTitles);
    }

    @Test
    void titlesFile_shouldContainOriginalAssessmentTitles() {
        assertTrue(canonicalTitles.contains("Architect"));
        assertTrue(canonicalTitles.contains("Software engineer"));
        assertTrue(canonicalTitles.contains("Quantity surveyor"));
        assertTrue(canonicalTitles.contains("Accountant"));
        assertTrue(
                canonicalTitles.size() > 4,
                "expected expanded vocabulary, size was " + canonicalTitles.size());
    }

    @ParameterizedTest(name = "{0} -> {1}")
    @CsvSource({
        // Original assessment examples
        "Java engineer, Software engineer",
        "C# engineer, Software engineer",
        "Accountant, Accountant",
        "Chief Accountant, Accountant",

        // Exact or near-exact titles from the file
        "Quantity surveyor, Quantity surveyor",
        "Financial analyst, Financial analyst",
        "Legal counsel, Legal counsel",

        // Titles with seniority or extra words
        "Senior product manager, Product manager",
        "Lead data scientist, Data scientist",
        "Junior UX designer, UX designer",
        "Staff accountant, Accountant",
        "Site quantity surveyor, Quantity surveyor",
        "Frontend software engineer, Software engineer",
        "Solutions architect, Architect",
        "Business systems analyst, Business analyst",

        // Looser matches that should still work
        "Marketing lead, Marketing manager",
        "Operations lead, Operations manager",
        "Graphic design specialist, Graphic designer",
        "Content writing lead, Content writer",
        "Customer support agent, Customer support specialist",
        "Sales rep, Sales representative"
    })
    void normalise_shouldMatchExpectedCanonicalTitle(String input, String expected) {
        NormalisationResult result = normaliser.normaliseWithQuality(input);

        assertEquals(
                expected,
                result.title(),
                () -> "input '" + input + "' scored q=" + result.quality());
        assertTrue(result.quality() > 0.0, "expected positive quality for '" + input + "'");
    }

    @Test
    void normalise_shouldPreferRelatedTitleOverUnrelatedTitle() {
        NormalisationResult product = normaliser.normaliseWithQuality("Senior product manager");
        NormalisationResult sales = normaliser.normaliseWithQuality("Enterprise sales representative");

        assertEquals("Product manager", product.title());
        assertEquals("Sales representative", sales.title());
        assertTrue(product.quality() > sales.quality() * 0.5);
    }
}
