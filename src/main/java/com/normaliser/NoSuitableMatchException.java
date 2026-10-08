package com.normaliser;

/**
 * Thrown when the best canonical match scores below the configured {@code minimumQuality}.
 *
 * <p>This is a business outcome (no suitable match), not invalid input — use {@link
 * IllegalArgumentException} for null/blank titles.
 */
public final class NoSuitableMatchException extends RuntimeException {

    private final String input;
    private final String bestTitle;
    private final double bestQuality;
    private final double minimumQuality;

    public NoSuitableMatchException(
            String input, String bestTitle, double bestQuality, double minimumQuality) {
        super(
                "No canonical title met the minimum quality of "
                        + minimumQuality
                        + " for input '"
                        + input
                        + "' (best was '"
                        + bestTitle
                        + "' with score "
                        + bestQuality
                        + ")");
        this.input = input;
        this.bestTitle = bestTitle;
        this.bestQuality = bestQuality;
        this.minimumQuality = minimumQuality;
    }

    public String input() {
        return input;
    }

    public String bestTitle() {
        return bestTitle;
    }

    public double bestQuality() {
        return bestQuality;
    }

    public double minimumQuality() {
        return minimumQuality;
    }
}
