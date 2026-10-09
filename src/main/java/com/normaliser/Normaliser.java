package com.normaliser;

import com.normaliser.scoring.DefaultScorer;
import com.normaliser.scoring.Scorer;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Finds the closest canonical job title for a free-form input string.
 *
 * <p>Uses a {@link Scorer} score from 0.0 to 1.0. By default the best match is always returned; set
 * {@code minimumScore} to reject weak matches.
 *
 * <p>{@code new Normaliser()} loads titles from {@code canonical-titles.txt}. You can also pass
 * your own title list. Titles are checked and prepared once when the normaliser is created.
 */
public final class Normaliser {

    private final List<CanonicalTitle> canonicalTitles;
    private final TitleNormaliser titleNormaliser;
    private final Scorer scorer;
    private final double minimumScore;

    /**
     * Loads the default titles from the classpath with no score floor.
     *
     * @throws UncheckedIOException if the titles file cannot be read
     */
    public Normaliser() {
        this(0.0);
    }

    /**
     * Loads the default titles from the classpath with a score floor.
     *
     * @param minimumScore minimum score required to accept a match ({@code 0.0}–{@code 1.0})
     * @throws UncheckedIOException if the titles file cannot be read
     */
    public Normaliser(double minimumScore) {
        this(loadDefaultCanonicalTitles(), new DefaultScorer(), minimumScore);
    }

    /** Uses the given titles, default scorer, and no score floor. */
    public Normaliser(Collection<String> canonicalTitles) {
        this(canonicalTitles, new DefaultScorer(), 0.0);
    }

    /** Uses the given titles and scorer, with no score floor. */
    public Normaliser(Collection<String> canonicalTitles, Scorer scorer) {
        this(canonicalTitles, scorer, 0.0);
    }

    /** Uses the given titles, scorer, and score floor. */
    public Normaliser(
            Collection<String> canonicalTitles, Scorer scorer, double minimumScore) {
        this(canonicalTitles, new TitleNormaliser(), scorer, minimumScore);
    }

    /** Full wiring used by all public constructors. */
    Normaliser(
            Collection<String> canonicalTitles,
            TitleNormaliser titleNormaliser,
            Scorer scorer,
            double minimumScore) {
        Objects.requireNonNull(canonicalTitles, "canonicalTitles must not be null");
        Objects.requireNonNull(titleNormaliser, "titleNormaliser must not be null");
        Objects.requireNonNull(scorer, "scorer must not be null");

        if (canonicalTitles.isEmpty()) {
            throw new IllegalArgumentException("canonicalTitles must not be empty");
        }
        if (Double.isNaN(minimumScore) || minimumScore < 0.0 || minimumScore > 1.0) {
            throw new IllegalArgumentException(
                    "minimumScore must be between 0.0 and 1.0 inclusive, but was: "
                            + minimumScore);
        }

        this.canonicalTitles = List.copyOf(prepareCanonicalTitles(canonicalTitles, titleNormaliser));
        this.titleNormaliser = titleNormaliser;
        this.scorer = scorer;
        this.minimumScore = minimumScore;
    }

    /**
     * Returns the closest canonical title for {@code jobTitle}.
     *
     * @param jobTitle the job title to normalise
     * @return the best-matching canonical title as stored in the list
     * @throws IllegalArgumentException if {@code jobTitle} is null or blank
     * @throws NoSuitableMatchException if the best score is below {@code minimumScore}
     */
    public String normalise(String jobTitle) {
        return normaliseWithScore(jobTitle).title();
    }

    /**
     * Like {@link #normalise(String)}, but also returns the similarity score.
     *
     * @throws IllegalArgumentException if {@code jobTitle} is null or blank
     * @throws NoSuitableMatchException if the best score is below {@code minimumScore}
     */
    public NormalisationResult normaliseWithScore(String jobTitle) {
        if (jobTitle == null) {
            throw new IllegalArgumentException("jobTitle must not be null");
        }
        if (jobTitle.isBlank()) {
            throw new IllegalArgumentException("jobTitle must not be blank");
        }

        String preparedInput = titleNormaliser.prepare(jobTitle);
        if (preparedInput.isBlank()) {
            throw new IllegalArgumentException(
                    "jobTitle must contain at least one meaningful character");
        }

        NormalisationResult best = null;
        for (CanonicalTitle candidate : canonicalTitles) {
            double score = scorer.score(preparedInput, candidate.comparisonForm());
            if (best == null || score > best.score()) {
                best = new NormalisationResult(candidate.displayForm(), score);
            }
        }

        Objects.requireNonNull(best, "canonicalTitles must yield at least one candidate");

        if (best.score() < minimumScore) {
            throw new NoSuitableMatchException(
                    jobTitle, best.title(), best.score(), minimumScore);
        }

        return best;
    }

    private static List<CanonicalTitle> prepareCanonicalTitles(
            Collection<String> canonicalTitles, TitleNormaliser titleNormaliser) {
        List<CanonicalTitle> prepared = new ArrayList<>(canonicalTitles.size());
        for (String title : canonicalTitles) {
            if (title == null || title.isBlank()) {
                throw new IllegalArgumentException("canonical titles must not be null or blank");
            }
            String comparisonForm = titleNormaliser.prepare(title);
            if (comparisonForm.isBlank()) {
                throw new IllegalArgumentException(
                        "canonical title must contain at least one meaningful character: '"
                                + title
                                + "'");
            }
            prepared.add(new CanonicalTitle(title, comparisonForm));
        }
        return prepared;
    }

    private static List<String> loadDefaultCanonicalTitles() {
        try {
            return CanonicalTitleRepository.load();
        } catch (IOException e) {
            throw new UncheckedIOException(
                    "Failed to load canonical titles from "
                            + CanonicalTitleRepository.DEFAULT_RESOURCE,
                    e);
        }
    }

    private record CanonicalTitle(String displayForm, String comparisonForm) {}
}
