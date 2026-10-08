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
 * {@code minimumQuality} to reject weak matches.
 *
 * <p>{@code new Normaliser()} loads titles from {@code canonical-titles.txt}. You can also pass
 * your own title list. Titles are checked and prepared once when the normaliser is created.
 */
public final class Normaliser {

    private final List<CanonicalTitle> canonicalTitles;
    private final TitleNormaliser titleNormaliser;
    private final Scorer scorer;
    private final double minimumQuality;

    /**
     * Loads the default titles from the classpath with no quality floor.
     *
     * @throws UncheckedIOException if the titles file cannot be read
     */
    public Normaliser() {
        this(0.0);
    }

    /**
     * Loads the default titles from the classpath with a quality floor.
     *
     * @param minimumQuality minimum score required to accept a match ({@code 0.0}–{@code 1.0})
     * @throws UncheckedIOException if the titles file cannot be read
     */
    public Normaliser(double minimumQuality) {
        this(loadDefaultCanonicalTitles(), new DefaultScorer(), minimumQuality);
    }

    /** Uses the given titles, default scorer, and no quality floor. */
    public Normaliser(Collection<String> canonicalTitles) {
        this(canonicalTitles, new DefaultScorer(), 0.0);
    }

    /** Uses the given titles and scorer, with no quality floor. */
    public Normaliser(Collection<String> canonicalTitles, Scorer scorer) {
        this(canonicalTitles, scorer, 0.0);
    }

    /** Uses the given titles, scorer, and quality floor. */
    public Normaliser(
            Collection<String> canonicalTitles, Scorer scorer, double minimumQuality) {
        this(canonicalTitles, new TitleNormaliser(), scorer, minimumQuality);
    }

    /** Full wiring used by all public constructors. */
    Normaliser(
            Collection<String> canonicalTitles,
            TitleNormaliser titleNormaliser,
            Scorer scorer,
            double minimumQuality) {
        Objects.requireNonNull(canonicalTitles, "canonicalTitles must not be null");
        Objects.requireNonNull(titleNormaliser, "titleNormaliser must not be null");
        Objects.requireNonNull(scorer, "scorer must not be null");

        if (canonicalTitles.isEmpty()) {
            throw new IllegalArgumentException("canonicalTitles must not be empty");
        }
        if (Double.isNaN(minimumQuality) || minimumQuality < 0.0 || minimumQuality > 1.0) {
            throw new IllegalArgumentException(
                    "minimumQuality must be between 0.0 and 1.0 inclusive, but was: "
                            + minimumQuality);
        }

        this.canonicalTitles = List.copyOf(prepareCanonicalTitles(canonicalTitles, titleNormaliser));
        this.titleNormaliser = titleNormaliser;
        this.scorer = scorer;
        this.minimumQuality = minimumQuality;
    }

    /**
     * Returns the closest canonical title for {@code jobTitle}.
     *
     * @param jobTitle the job title to normalise
     * @return the best-matching canonical title as stored in the list
     * @throws IllegalArgumentException if {@code jobTitle} is null or blank
     * @throws NoSuitableMatchException if the best score is below {@code minimumQuality}
     */
    public String normalise(String jobTitle) {
        return normaliseWithQuality(jobTitle).title();
    }

    /**
     * Like {@link #normalise(String)}, but also returns the similarity score.
     *
     * @throws IllegalArgumentException if {@code jobTitle} is null or blank
     * @throws NoSuitableMatchException if the best score is below {@code minimumQuality}
     */
    public NormalisationResult normaliseWithQuality(String jobTitle) {
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
            double quality = scorer.score(preparedInput, candidate.comparisonForm());
            if (best == null || quality > best.quality()) {
                best = new NormalisationResult(candidate.displayForm(), quality);
            }
        }

        Objects.requireNonNull(best, "canonicalTitles must yield at least one candidate");

        if (best.quality() < minimumQuality) {
            throw new NoSuitableMatchException(
                    jobTitle, best.title(), best.quality(), minimumQuality);
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
