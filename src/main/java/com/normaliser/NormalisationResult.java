package com.normaliser;

/**
 * Result of normalising a job title: the chosen canonical title and how similar it was.
 *
 * @param title the canonical title as stored (display form)
 * @param score similarity score from 0.0 to 1.0
 */
public record NormalisationResult(String title, double score) {

    public NormalisationResult {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title must not be null or blank");
        }
        if (Double.isNaN(score) || score < 0.0 || score > 1.0) {
            throw new IllegalArgumentException(
                    "score must be between 0.0 and 1.0 inclusive, but was: " + score);
        }
    }
}
