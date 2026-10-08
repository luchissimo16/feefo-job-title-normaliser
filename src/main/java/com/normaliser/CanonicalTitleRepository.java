package com.normaliser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Reads canonical job titles from a text file (one title per line). Skips blank lines and lines
 * that start with {@code #}.
 */
public final class CanonicalTitleRepository {

    /** Default titles file on the classpath ({@code src/main/resources}). */
    public static final String DEFAULT_RESOURCE = "canonical-titles.txt";

    private CanonicalTitleRepository() {}

    /** Loads titles from the default classpath file. */
    public static List<String> load() throws IOException {
        return loadResource(DEFAULT_RESOURCE);
    }

    /**
     * Loads titles from a classpath resource.
     *
     * @param resourceName resource name (must not be null)
     * @return at least one trimmed title
     * @throws IllegalArgumentException if the resource is missing or has no titles
     * @throws IOException if the resource cannot be read
     */
    public static List<String> loadResource(String resourceName) throws IOException {
        Objects.requireNonNull(resourceName, "resourceName must not be null");

        InputStream stream =
                CanonicalTitleRepository.class.getClassLoader().getResourceAsStream(resourceName);
        if (stream == null) {
            throw new IllegalArgumentException(
                    "titles resource not found on classpath: " + resourceName);
        }

        try (BufferedReader reader =
                new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            return parseLines(reader.lines().toList(), resourceName);
        }
    }

    /**
     * Loads titles from a file on disk.
     *
     * @param path path to the titles file (must not be null)
     * @return at least one trimmed title
     * @throws IllegalArgumentException if the file is missing, not a normal file, or has no titles
     * @throws IOException if the file cannot be read
     */
    public static List<String> load(Path path) throws IOException {
        Objects.requireNonNull(path, "path must not be null");

        if (!Files.isRegularFile(path)) {
            throw new IllegalArgumentException(
                    "titles file does not exist or is not a regular file: " + path);
        }

        return parseLines(Files.readAllLines(path), path.toString());
    }

    private static List<String> parseLines(List<String> lines, String source) {
        List<String> titles = new ArrayList<>();
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                continue;
            }
            titles.add(trimmed);
        }

        if (titles.isEmpty()) {
            throw new IllegalArgumentException(
                    "titles file must contain at least one title: " + source);
        }

        return List.copyOf(titles);
    }
}
