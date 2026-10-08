package com.normaliser;

import java.util.Locale;

/**
 * Turns a job title into a simple form used for comparison. Does not change how titles are shown to
 * the user — {@link Normaliser} keeps the original display text separately.
 */
public final class TitleNormaliser {

    /**
     * Prepares {@code title} for comparison.
     *
     * <p>Trims, lowercases, replaces punctuation with spaces (keeps {@code #} so {@code C#} stays
     * intact), then collapses repeated spaces to one.
     *
     * @param title the raw title (must not be null)
     * @return the comparison form (may be blank if the input had no useful characters)
     * @throws IllegalArgumentException if {@code title} is null
     */
    public String prepare(String title) {
        if (title == null) {
            throw new IllegalArgumentException("title must not be null");
        }

        String normalised = title.trim().toLowerCase(Locale.ROOT);
        normalised = replacePunctuation(normalised);
        return collapseWhitespace(normalised);
    }

    private static String replacePunctuation(String value) {
        StringBuilder builder = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char character = value.charAt(i);
            if (Character.isLetterOrDigit(character)
                    || character == '#'
                    || Character.isWhitespace(character)) {
                builder.append(character);
            } else {
                builder.append(' ');
            }
        }
        return builder.toString();
    }

    private static String collapseWhitespace(String value) {
        return value.replaceAll("\\s+", " ").trim();
    }
}
