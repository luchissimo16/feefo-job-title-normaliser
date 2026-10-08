package com.normaliser.scoring;

/**
 * Scores how similar two prepared title strings are.
 *
 * <p>The score must be between 0.0 and 1.0. {@code 1.0} means the titles match exactly.
 */
@FunctionalInterface
public interface Scorer {

    /**
     * @param input the prepared input title
     * @param candidate the prepared candidate title
     * @return a score from 0.0 to 1.0
     */
    double score(String input, String candidate);
}
