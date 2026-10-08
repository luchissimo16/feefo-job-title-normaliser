package com.normaliser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Loading titles from files and the default classpath resource. */
class CanonicalTitleRepositoryTest {

    @TempDir Path tempDir;

    @Test
    void load_shouldReadTitlesOnePerLine() throws IOException {
        Path file = tempDir.resolve("titles.txt");
        Files.writeString(file, "Architect\nSoftware engineer\nAccountant\n");

        assertEquals(
                List.of("Architect", "Software engineer", "Accountant"),
                CanonicalTitleRepository.load(file));
    }

    @Test
    void load_shouldIgnoreBlankLinesAndComments() throws IOException {
        Path file = tempDir.resolve("titles.txt");
        Files.writeString(
                file,
                """
                # Canonical titles
                Architect

                # engineering
                Software engineer
                """);

        assertEquals(List.of("Architect", "Software engineer"), CanonicalTitleRepository.load(file));
    }

    @Test
    void load_shouldTrimWhitespace() throws IOException {
        Path file = tempDir.resolve("titles.txt");
        Files.writeString(file, "  Accountant  \n");

        assertEquals(List.of("Accountant"), CanonicalTitleRepository.load(file));
    }

    @Test
    void load_shouldRejectNullPath() {
        assertThrows(NullPointerException.class, () -> CanonicalTitleRepository.load(null));
    }

    @Test
    void load_shouldRejectMissingFile() {
        Path missing = tempDir.resolve("does-not-exist.txt");

        assertThrows(IllegalArgumentException.class, () -> CanonicalTitleRepository.load(missing));
    }

    @Test
    void load_shouldRejectEmptyFile() throws IOException {
        Path file = tempDir.resolve("empty.txt");
        Files.writeString(file, "# only comments\n\n");

        assertThrows(IllegalArgumentException.class, () -> CanonicalTitleRepository.load(file));
    }

    @Test
    void load_shouldReadDefaultCanonicalTitlesResource() throws IOException {
        List<String> titles = CanonicalTitleRepository.load();

        assertTrue(titles.contains("Architect"));
        assertTrue(titles.contains("Software engineer"));
        assertTrue(titles.contains("Quantity surveyor"));
        assertTrue(titles.contains("Accountant"));
        assertTrue(titles.size() > 4);
    }

    @Test
    void loadResource_shouldRejectMissingResource() {
        assertThrows(
                IllegalArgumentException.class,
                () -> CanonicalTitleRepository.loadResource("does-not-exist.txt"));
    }
}
