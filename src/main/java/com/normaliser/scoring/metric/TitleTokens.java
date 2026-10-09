package com.normaliser.scoring.metric;

import java.util.HashSet;
import java.util.Set;

/** Breaks a prepared title into words, e.g. {@code "java engineer"} → {java, engineer}. */
final class TitleTokens {

    private TitleTokens() {}

    static Set<String> of(String value) {
        if (value == null) {
            throw new IllegalArgumentException("value must not be null");
        }
        if (value.isEmpty()) {
            return Set.of();
        }

        Set<String> tokens = new HashSet<>();
        for (String token : value.split(" ")) {
            if (!token.isEmpty()) {
                tokens.add(token);
            }
        }
        return tokens;
    }
}
