# Job Title Normaliser

Maps a free-form job title to the closest title from a configured list of
canonical titles, using a similarity score from `0.0` to `1.0`.

Canonical titles live in
[`src/main/resources/canonical-titles.txt`](src/main/resources/canonical-titles.txt)
(one title per line; blank lines and `#` comments are ignored).
`new Normaliser()` loads that file. You can also pass your own list with
`new Normaliser(titles)`.

## Requirements

- Java 25+
- Maven 3.10+ (or use the included wrapper: `./mvnw`)

## Build and test

```bash
./mvnw clean package
./mvnw test
```

## Run

```bash
./mvnw -q -DskipTests package

java -cp target/classes com.normaliser.NormaliserCli "Java engineer" "Accountant"
```

If no arguments are given, the CLI prints usage and exits with status `1`.
Blank or punctuation-only titles are passed to `Normaliser` and fail with
`IllegalArgumentException` (message printed, exit status `1`).

## How it works

1. **Load titles** — `CanonicalTitleRepository` reads the titles file.
2. **Prepare text** — `TitleNormaliser` trims, lowercases, and cleans
   punctuation (keeps `#` so `C#` stays intact). Display text is unchanged.
3. **Score** — `DefaultScorer` compares the input to each canonical title and
   returns a score from `0.0` to `1.0`.
4. **Pick the best** — `Normaliser` returns the highest-scoring title.

Main classes:

| Class | Role |
| --- | --- |
| `Normaliser` | Matching logic |
| `CanonicalTitleRepository` | Provides canonical titles from file |
| `TitleNormaliser` | Prepares text for comparison |
| `Scorer` / `DefaultScorer` | Scoring composition (`com.normaliser.scoring`) |
| Metric `Scorer`s | Individual metrics (`com.normaliser.scoring.metric`) |
| `NormaliserCli` | Command-line wiring |

Titles are prepared once when `Normaliser` is created.

### Scoring

For prepared input `I` and candidate `C`: exact match → `1.0`. Otherwise
`DefaultScorer` sums weighted metric `Scorer`s (then clamps to `[0.0, 1.0]`).
Default wiring:

- `Jaccard` — shared words / all words (`0.35`)
- `Coverage` — shared words / smaller title (`0.35`)
- `Containment` — one title contains the other (`0.20`)
- `CharacterSimilarity` — Levenshtein edit similarity (`0.10`)

Add, remove, or reweight metrics by changing the list in `DefaultScorer` (or
passing a custom `List<WeightedScorer>`). Weights are chosen for this assessment;
there are no special-case mappings for “Java” or “C#”.

## Assumptions

- **Low-confidence matches:** with the default `minimumQuality` of `0.0`, the
  best-scoring title is always returned, even for weak inputs. Callers can raise
  the floor in code, e.g. `new Normaliser(0.5)` or
  `new Normaliser(titles, scorer, 0.5)`, to reject weak matches
  (`NoSuitableMatchException`).
- **Ties:** if two titles share the same top score, the first one in the list
  wins.
- **Invalid input:** `null`, blank strings, and titles with no meaningful
  characters after prep (e.g. `"----"`) throw `IllegalArgumentException`.
