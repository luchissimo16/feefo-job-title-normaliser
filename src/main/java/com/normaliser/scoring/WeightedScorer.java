package com.normaliser.scoring;

import java.util.Objects;

/**
 * A {@link Scorer} paired with a non-negative weight used by {@link DefaultScorer}.
 */
public record WeightedScorer(Scorer scorer, double weight) {

    public WeightedScorer {
        Objects.requireNonNull(scorer, "scorer must not be null");
        if (Double.isNaN(weight) || weight < 0.0) {
            throw new IllegalArgumentException(
                    "weight must be a non-negative number, but was: " + weight);
        }
    }
}
