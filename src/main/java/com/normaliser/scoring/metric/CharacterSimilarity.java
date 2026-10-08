package com.normaliser.scoring.metric;

import com.normaliser.scoring.Scorer;

/**
 * How close the characters are (typos / small edits).
 *
 * <p>Uses edit distance: similarity = 1 − (edits / length of the longer string).
 */
public final class CharacterSimilarity implements Scorer {

    @Override
    public double score(String input, String candidate) {
        int maxLength = Math.max(input.length(), candidate.length());
        if (maxLength == 0) {
            return 1.0;
        }
        int distance = levenshteinDistance(input, candidate);
        return 1.0 - ((double) distance / maxLength);
    }

    /** Counts insert/delete/replace edits needed to change one string into the other. */
    public static int levenshteinDistance(String left, String right) {
        int leftLength = left.length();
        int rightLength = right.length();

        int[] previous = new int[rightLength + 1];
        int[] current = new int[rightLength + 1];

        for (int j = 0; j <= rightLength; j++) {
            previous[j] = j;
        }

        for (int i = 1; i <= leftLength; i++) {
            current[0] = i;
            char leftChar = left.charAt(i - 1);
            for (int j = 1; j <= rightLength; j++) {
                int insertion = current[j - 1] + 1;
                int deletion = previous[j] + 1;
                int substitution = previous[j - 1] + (leftChar == right.charAt(j - 1) ? 0 : 1);
                current[j] = Math.min(insertion, Math.min(deletion, substitution));
            }
            int[] swap = previous;
            previous = current;
            current = swap;
        }

        return previous[rightLength];
    }
}
