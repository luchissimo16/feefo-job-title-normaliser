package com.normaliser;

/**
 * Result of normalising a job title: the chosen canonical title and how similar it was.
 *
 * @param title the canonical title as stored (display form)
 * @param quality similarity score from 0.0 to 1.0
 */
public record NormalisationResult(String title, double quality) {

    public NormalisationResult {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title must not be null or blank");
        }
        if (Double.isNaN(quality) || quality < 0.0 || quality > 1.0) {
            throw new IllegalArgumentException(
                    "quality must be between 0.0 and 1.0 inclusive, but was: " + quality);
        }
    }
}
