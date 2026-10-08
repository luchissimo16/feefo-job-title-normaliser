package com.normaliser.scoring.metric;

import com.normaliser.scoring.Scorer;
import java.util.Set;

/**
 * Word overlap vs all words used in either title.
 *
 * <p>Example: {java, engineer} vs {software, engineer} → 1 shared / 3 unique = 0.33.
 */
public final class Jaccard implements Scorer {

    @Override
    public double score(String input, String candidate) {
        return score(TitleTokens.of(input), TitleTokens.of(candidate));
    }

    static double score(Set<String> left, Set<String> right) {
        if (left.isEmpty() && right.isEmpty()) {
            return 1.0;
        }
        if (left.isEmpty() || right.isEmpty()) {
            return 0.0;
        }

        int intersection = 0;
        for (String token : left) {
            if (right.contains(token)) {
                intersection++;
            }
        }
        int union = left.size() + right.size() - intersection;
        return union == 0 ? 0.0 : (double) intersection / union;
    }
}
