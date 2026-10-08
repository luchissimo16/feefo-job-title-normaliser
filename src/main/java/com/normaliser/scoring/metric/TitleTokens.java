package com.normaliser.scoring.metric;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/** Breaks a prepared title into words, e.g. {@code "java engineer"} → {java, engineer}. */
final class TitleTokens {

    private TitleTokens() {}

    static Set<String> of(String value) {
        if (value.isEmpty()) {
            return Set.of();
        }
        return new HashSet<>(Arrays.asList(value.split(" ")));
    }
}
