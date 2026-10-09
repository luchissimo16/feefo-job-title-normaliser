package com.normaliser.scoring;

import com.normaliser.scoring.metric.CharacterSimilarity;
import com.normaliser.scoring.metric.Containment;
import com.normaliser.scoring.metric.Coverage;
import com.normaliser.scoring.metric.Jaccard;
import java.util.List;
import java.util.Objects;

/**
 * Scores how similar two prepared titles are (0.0 = unrelated, 1.0 = identical).
 *
 * <p>How the final score is built:
 *
 * <ol>
 *   <li>Same string → {@code 1.0}
 *   <li>Either string empty → {@code 0.0}
 *   <li>Otherwise sum {@code weight * scorer} for each wired {@link WeightedScorer}, then clamp to
 *       {@code [0.0, 1.0]}
 * </ol>
 *
 * <p>Default weights: {@code 0.35} Jaccard, {@code 0.35} coverage, {@code 0.20} containment, {@code
 * 0.10} character similarity. Pass a custom list to add, remove, or reweight metrics.
 *
 * <p>Inputs should already be prepared (see {@code TitleNormaliser}). Thread-safe for concurrent
 * {@link #score} calls (stateless scorers, immutable scorer list).
 */
public final class DefaultScorer implements Scorer {

    private final List<WeightedScorer> scorers;

    public DefaultScorer() {
        this(
                List.of(
                        new WeightedScorer(new Jaccard(), 0.35),
                        new WeightedScorer(new Coverage(), 0.35),
                        new WeightedScorer(new Containment(), 0.20),
                        new WeightedScorer(new CharacterSimilarity(), 0.10)));
    }

    public DefaultScorer(List<WeightedScorer> scorers) {
        Objects.requireNonNull(scorers, "scorers must not be null");
        if (scorers.isEmpty()) {
            throw new IllegalArgumentException("scorers must not be empty");
        }
        this.scorers = List.copyOf(scorers);
    }

    @Override
    public double score(String input, String candidate) {
        if (input == null) {
            throw new IllegalArgumentException("input must not be null");
        }
        if (candidate == null) {
            throw new IllegalArgumentException("candidate must not be null");
        }

        if (input.equals(candidate)) {
            return 1.0;
        }
        if (input.isEmpty() || candidate.isEmpty()) {
            return 0.0;
        }

        double weighted = 0.0;
        for (WeightedScorer weightedScorer : scorers) {
            weighted += weightedScorer.weight() * weightedScorer.scorer().score(input, candidate);
        }

        return Math.clamp(weighted, 0.0, 1.0);
    }
}
