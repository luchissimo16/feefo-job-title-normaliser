package com.normaliser.scoring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.normaliser.scoring.metric.CharacterSimilarity;
import com.normaliser.scoring.metric.Containment;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Score bounds, ranking of related titles, composition, and Levenshtein helpers. */
class DefaultScorerTest {

    private DefaultScorer scorer;

    @BeforeEach
    void setUp() {
        scorer = new DefaultScorer();
    }

    // Score range and ranking

    @Test
    void score_shouldReturnOneForIdenticalTitles() {
        assertEquals(1.0, scorer.score("software engineer", "software engineer"));
    }

    @Test
    void score_shouldNeverFallBelowZero() {
        double score = scorer.score("zzz", "accountant");
        assertTrue(score >= 0.0, "score was " + score);
    }

    @Test
    void score_shouldNeverExceedOne() {
        double score = scorer.score("software engineer", "software engineer");
        assertTrue(score <= 1.0, "score was " + score);
    }

    @Test
    void score_shouldRejectNullInput() {
        assertThrows(IllegalArgumentException.class, () -> scorer.score(null, "accountant"));
    }

    @Test
    void score_shouldRejectNullCandidate() {
        assertThrows(IllegalArgumentException.class, () -> scorer.score("accountant", null));
    }

    @Test
    void score_shouldReturnZeroWhenEitherSideIsEmpty() {
        assertEquals(0.0, scorer.score("", "accountant"));
        assertEquals(0.0, scorer.score("accountant", ""));
    }

    @Test
    void score_shouldPreferRelatedCandidateOverUnrelatedCandidate() {
        double related = scorer.score("java engineer", "software engineer");
        double unrelated = scorer.score("java engineer", "accountant");

        assertTrue(
                related > unrelated,
                "expected related score " + related + " > unrelated score " + unrelated);
    }

    @Test
    void score_shouldPreferAccountantOverArchitectForChiefAccountant() {
        double accountant = scorer.score("chief accountant", "accountant");
        double architect = scorer.score("chief accountant", "architect");

        assertTrue(
                accountant > architect,
                "expected accountant score " + accountant + " > architect score " + architect);
    }

    @Test
    void score_shouldScoreHighWhenOneTitleContainsTheOther() {
        double contained = scorer.score("chief accountant", "accountant");
        double unrelated = scorer.score("chief accountant", "software engineer");

        assertTrue(
                contained > unrelated,
                "expected contained score " + contained + " > unrelated score " + unrelated);
        assertTrue(contained > 0.5, "expected strong score when one title contains the other");
    }

    @Test
    void score_shouldPreferSoftwareEngineerForCsharpEngineer() {
        double softwareEngineer = scorer.score("c# engineer", "software engineer");
        double quantitySurveyor = scorer.score("c# engineer", "quantity surveyor");

        assertTrue(
                softwareEngineer > quantitySurveyor,
                "expected software engineer score "
                        + softwareEngineer
                        + " > quantity surveyor score "
                        + quantitySurveyor);
    }

    @Test
    void score_shouldSupportCustomScorerList() {
        DefaultScorer containmentOnly =
                new DefaultScorer(List.of(new WeightedScorer(new Containment(), 1.0)));

        double contained = containmentOnly.score("chief accountant", "accountant");
        double unrelated = containmentOnly.score("chief accountant", "software engineer");

        assertEquals(1.0, contained);
        assertEquals(0.0, unrelated);
    }

    // Levenshtein unit checks

    @Test
    void levenshteinDistance_shouldBeZeroForIdenticalStrings() {
        assertEquals(0, CharacterSimilarity.levenshteinDistance("abc", "abc"));
    }

    @Test
    void levenshteinDistance_shouldCountSingleEdit() {
        assertEquals(1, CharacterSimilarity.levenshteinDistance("cat", "bat"));
    }
}
