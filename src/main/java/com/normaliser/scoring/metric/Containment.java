package com.normaliser.scoring.metric;

import com.normaliser.scoring.Scorer;

/**
 * Full-string check: does one title contain the other as text?
 *
 * <p>Example: {@code "chief accountant"} contains {@code "accountant"} → 1.0; otherwise 0.0.
 */
public final class Containment implements Scorer {

    @Override
    public double score(String input, String candidate) {
        return input.contains(candidate) || candidate.contains(input) ? 1.0 : 0.0;
    }
}
