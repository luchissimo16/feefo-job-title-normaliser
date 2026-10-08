package com.normaliser;

/**
 * Command-line entry point. Wires input to {@link Normaliser}; it does not do matching itself.
 *
 * <p>Canonical titles come from {@code canonical-titles.txt}. Job titles to normalise come from
 * command-line args.
 */
public final class NormaliserCli {

    private NormaliserCli() {}

    public static void main(String[] args) {
        try {
            if (args.length == 0) {
                printUsage();
                System.exit(1);
            }

            Normaliser normaliser = new Normaliser();
            for (String input : args) {
                System.out.println(input + " -> " + normaliser.normalise(input));
            }
        } catch (RuntimeException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    private static void printUsage() {
        System.err.println("Usage: NormaliserCli <job title>...");
        System.err.println(
                "Canonical titles are read from classpath:"
                        + CanonicalTitleRepository.DEFAULT_RESOURCE);
    }
}
