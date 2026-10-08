package com.normaliser.scoring.metric;

import com.normaliser.scoring.Scorer;
import java.util.Set;

/**
 * How completely the smaller title's words appear in the other.
 *
 * <p>Example: {chief, accountant} vs {accountant} → 1 shared / 1 (smaller) = 1.0.
 */
public final class Coverage implements Scorer {

    @Override
    public double score(String input, String candidate) {
        return score(TitleTokens.of(input), TitleTokens.of(candidate));
    }

    static double score(Set<String> left, Set<String> right) {
        if (left.isEmpty() || right.isEmpty()) {
            return 0.0;
        }

        int intersection = 0;
        for (String token : left) {
            if (right.contains(token)) {
                intersection++;
            }
        }
        int smaller = Math.min(left.size(), right.size());
        return (double) intersection / smaller;
    }
}
