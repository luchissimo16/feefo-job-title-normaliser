package com.normaliser;

/**
 * Thrown when the best canonical match scores below the configured {@code minimumScore}.
 *
 * <p>This is a business outcome (no suitable match), not invalid input — use {@link
 * IllegalArgumentException} for null/blank titles.
 */
public final class NoSuitableMatchException extends RuntimeException {

    private final String input;
    private final String bestTitle;
    private final double bestScore;
    private final double minimumScore;

    public NoSuitableMatchException(
            String input, String bestTitle, double bestScore, double minimumScore) {
        super(buildMessage(input, bestTitle, bestScore, minimumScore));
        this.input = input;
        this.bestTitle = bestTitle;
        this.bestScore = bestScore;
        this.minimumScore = minimumScore;
    }

    private static String buildMessage(
            String input, String bestTitle, double bestScore, double minimumScore) {
        if (input == null) {
            throw new IllegalArgumentException("input must not be null");
        }
        if (bestTitle == null || bestTitle.isBlank()) {
            throw new IllegalArgumentException("bestTitle must not be null or blank");
        }
        if (Double.isNaN(bestScore) || bestScore < 0.0 || bestScore > 1.0) {
            throw new IllegalArgumentException(
                    "bestScore must be between 0.0 and 1.0 inclusive, but was: " + bestScore);
        }
        if (Double.isNaN(minimumScore) || minimumScore < 0.0 || minimumScore > 1.0) {
            throw new IllegalArgumentException(
                    "minimumScore must be between 0.0 and 1.0 inclusive, but was: " + minimumScore);
        }
        return "No canonical title met the minimum score of "
                + minimumScore
                + " for input '"
                + input
                + "' (best was '"
                + bestTitle
                + "' with score "
                + bestScore
                + ")";
    }

    public String input() {
        return input;
    }

    public String bestTitle() {
        return bestTitle;
    }

    public double bestScore() {
        return bestScore;
    }

    public double minimumScore() {
        return minimumScore;
    }
}
