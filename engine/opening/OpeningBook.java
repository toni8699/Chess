package engine.opening;

import engine.Alliance;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Lazy-loading opening book backed by the {@code OpeningDatabase.csv} file.
 * The book is aggregated into a prefix tree so lookups run in O(depth) time and
 * can be shared safely between multiple game sessions.
 */
public final class OpeningBook {

    private static final Logger LOGGER = Logger.getLogger(OpeningBook.class.getName());
    private static final String DEFAULT_DATABASE = "OpeningDatabase.csv";

    private final OpeningBookNode root;
    private final int entryCount;

    private OpeningBook(final OpeningBookNode root, final int entryCount) {
        this.root = root;
        this.entryCount = entryCount;
    }

    public static OpeningBook getInstance() {
        return Holder.INSTANCE;
    }

    public int getEntryCount() {
        return entryCount;
    }

    public PathTracker newTracker(final Alliance sideToMove) {
        return new PathTracker(root, sideToMove);
    }

    /**
     * Tracks the current prefix within the opening tree for a single game.
     */
    public static final class PathTracker {

        private final OpeningBookNode root;
        private OpeningBookNode current;
        private Alliance sideToMove;

        private PathTracker(final OpeningBookNode root, final Alliance sideToMove) {
            this.root = Objects.requireNonNull(root);
            this.current = root;
            this.sideToMove = Objects.requireNonNull(sideToMove);
        }

        public boolean isInBook() {
            return current != null && !current.childrenStats.isEmpty();
        }

        public Optional<OpeningSuggestion> bestSuggestion() {
            final List<OpeningSuggestion> suggestions = suggestions();
            if (suggestions.isEmpty()) {
                return Optional.empty();
            }
            double totalWeight = 0.0;
            for (final OpeningSuggestion suggestion : suggestions) {
                totalWeight += Math.max(1, suggestion.stats().totalGames());
            }
            if (totalWeight <= 0.0) {
                return Optional.of(suggestions.get(ThreadLocalRandom.current().nextInt(suggestions.size())));
            }
            double pick = ThreadLocalRandom.current().nextDouble(totalWeight);
            for (final OpeningSuggestion suggestion : suggestions) {
                pick -= Math.max(1, suggestion.stats().totalGames());
                if (pick <= 0.0) {
                    return Optional.of(suggestion);
                }
            }
            return Optional.of(suggestions.get(suggestions.size() - 1));
        }

        public List<OpeningSuggestion> suggestions() {
            if (current == null) {
                return List.of();
            }
            final List<OpeningSuggestion> moves = new ArrayList<>(current.childrenStats.size());
            for (final Map.Entry<String, OpeningStats> entry : current.childrenStats.entrySet()) {
                moves.add(new OpeningSuggestion(entry.getKey(), entry.getValue()));
            }
            moves.sort(Comparator
                    .comparingDouble((OpeningSuggestion suggestion) -> suggestion.stats().expectedScore()).reversed()
                    .thenComparingInt(suggestion -> suggestion.stats().totalGames()).reversed()
                    .thenComparing(OpeningSuggestion::san));
            return Collections.unmodifiableList(moves);
        }

        public void recordSan(final String san) {
            if (current == null) {
                return;
            }
            final OpeningBookNode next = current.children.get(san);
            if (next == null) {
                current = null;
                return;
            }
            current = next;
            sideToMove = sideToMove.getOppositeAlliance();
        }

        public void reset(final Alliance initialSide) {
            this.current = root;
            this.sideToMove = Objects.requireNonNull(initialSide);
        }

        public Alliance getSideToMove() {
            return sideToMove;
        }
    }

    public record OpeningSuggestion(String san, OpeningStats stats) {
    }

    public static final class OpeningStats {
        private int totalGames;
        private double expectedScoreSum;
        private double winPercentSum;
        private double drawPercentSum;

        private void addSample(final int games,
                               final double expectedScore,
                               final double winPercent,
                               final double drawPercent) {
            if (games <= 0) {
                return;
            }
            this.totalGames += games;
            this.expectedScoreSum += expectedScore * games;
            this.winPercentSum += winPercent * games;
            this.drawPercentSum += drawPercent * games;
        }

        public int totalGames() {
            return totalGames;
        }

        public double expectedScore() {
            if (totalGames == 0) {
                return 0.5;
            }
            return expectedScoreSum / totalGames;
        }

        public double winPercent() {
            if (totalGames == 0) {
                return 0.0;
            }
            return winPercentSum / totalGames;
        }

        public double drawPercent() {
            if (totalGames == 0) {
                return 0.0;
            }
            return drawPercentSum / totalGames;
        }
    }

    private static final class OpeningBookNode {
        private final Map<String, OpeningBookNode> children = new HashMap<>();
        private final Map<String, OpeningStats> childrenStats = new HashMap<>();

        private OpeningBookNode childFor(final String san) {
            return children.computeIfAbsent(san, ignored -> new OpeningBookNode());
        }

        private OpeningStats statsFor(final String san) {
            return childrenStats.computeIfAbsent(san, ignored -> new OpeningStats());
        }
    }

    private static final class Holder {
        private static final OpeningBook INSTANCE = loadBook();
    }

    private static OpeningBook loadBook() {
        try {
            final Loader loader = new Loader();
            return loader.load();
        } catch (final IOException e) {
            LOGGER.log(Level.WARNING, "Failed to load opening database: {0}", e.getMessage());
            return new OpeningBook(new OpeningBookNode(), 0);
        }
    }

    private static final class Loader {

        private static final int MIN_FIELD_COUNT = 23;
        private static final int INDEX_NUM_GAMES = 3;
        private static final int INDEX_DRAW_PERCENT = 9;
        private static final int INDEX_MOVES_LIST = 12;
        private static final int INDEX_WHITE_WIN_PERCENT = 21;
        private static final int INDEX_BLACK_WIN_PERCENT = 22;

        private final OpeningBookNode root = new OpeningBookNode();
        private int rowsLoaded;

        private OpeningBook load() throws IOException {
            try (BufferedReader reader = openCsv()) {
                // Skip header
                reader.readLine();
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.isBlank()) {
                        continue;
                    }
                    final List<String> fields = parseCsvLine(line);
                    if (fields.size() <= MIN_FIELD_COUNT) {
                        continue;
                    }
                    final OpeningEntry entry = toEntry(fields);
                    if (entry.sanMoves().isEmpty()) {
                        continue;
                    }
                    addEntry(entry);
                    rowsLoaded++;
                }
            }
            LOGGER.info(() -> "Loaded " + rowsLoaded + " opening rows into cache");
            return new OpeningBook(root, rowsLoaded);
        }

        private BufferedReader openCsv() throws IOException {
            final Path path = resolveCsvPath();
            if (path != null && Files.exists(path)) {
                return Files.newBufferedReader(path, StandardCharsets.UTF_8);
            }
            final var resourceStream = OpeningBook.class.getResourceAsStream("/OpeningDatabase.csv");
            if (resourceStream != null) {
                return new BufferedReader(new java.io.InputStreamReader(resourceStream, StandardCharsets.UTF_8));
            }
            throw new IOException("Opening database not found. Expected at "
                    + (path == null ? "<unspecified>" : path.toAbsolutePath())
                    + " or on the classpath as /OpeningDatabase.csv");
        }

        private void addEntry(final OpeningEntry entry) {
            OpeningBookNode node = root;
            Alliance side = Alliance.WHITE;
            for (final String san : entry.sanMoves()) {
                final OpeningBookNode child = node.childFor(san);
                final OpeningStats stats = node.statsFor(san);
                stats.addSample(entry.numGames(),
                        entry.expectedScore(side),
                        entry.winPercent(side),
                        entry.drawPercent());
                node = child;
                side = side.getOppositeAlliance();
            }
        }

        private OpeningEntry toEntry(final List<String> fields) {
            final int numGames = parseInt(fields.get(INDEX_NUM_GAMES), 0);
            final double drawPercent = parseDouble(fields.get(INDEX_DRAW_PERCENT), 0.0);
            final double whiteWin = parseDouble(fields.get(INDEX_WHITE_WIN_PERCENT), 0.0);
            final double blackWin = parseDouble(fields.get(INDEX_BLACK_WIN_PERCENT), 0.0);
            final List<String> sanMoves = parseMovesList(fields.get(INDEX_MOVES_LIST));
            final String name = fields.size() > 1 ? fields.get(1) : "";
            return new OpeningEntry(name, numGames, sanMoves, whiteWin, blackWin, drawPercent);
        }

        private static Path resolveCsvPath() {
            final String override = System.getProperty("chess.opening.csv");
            if (override != null && !override.isBlank()) {
                final Path overridePath = Paths.get(override);
                if (Files.exists(overridePath)) {
                    return overridePath;
                }
            }
            return Paths.get(DEFAULT_DATABASE);
        }

        private static List<String> parseCsvLine(final String line) {
            final List<String> fields = new ArrayList<>();
            final StringBuilder buffer = new StringBuilder();
            boolean inQuotes = false;
            for (int i = 0; i < line.length(); i++) {
                final char ch = line.charAt(i);
                if (ch == '"') {
                    inQuotes = !inQuotes;
                } else if (ch == ',' && !inQuotes) {
                    fields.add(buffer.toString());
                    buffer.setLength(0);
                } else {
                    buffer.append(ch);
                }
            }
            fields.add(buffer.toString());
            return fields;
        }

        private static List<String> parseMovesList(final String raw) {
            if (raw == null || raw.isBlank()) {
                return List.of();
            }
            String trimmed = raw.trim();
            if (trimmed.startsWith("[")) {
                trimmed = trimmed.substring(1);
            }
            if (trimmed.endsWith("]")) {
                trimmed = trimmed.substring(0, trimmed.length() - 1);
            }
            if (trimmed.isBlank()) {
                return List.of();
            }
            final String[] tokens = trimmed.split(",");
            final List<String> moves = new ArrayList<>(tokens.length);
            for (String token : tokens) {
                String sanitized = token.trim();
                if (sanitized.isEmpty()) {
                    continue;
                }
                if (sanitized.startsWith("'") && sanitized.endsWith("'") && sanitized.length() >= 2) {
                    sanitized = sanitized.substring(1, sanitized.length() - 1);
                }
                sanitized = sanitized.trim();
                if (sanitized.isEmpty()) {
                    continue;
                }
                final int dotIndex = sanitized.indexOf('.');
                if (dotIndex >= 0 && dotIndex < sanitized.length() - 1) {
                    sanitized = sanitized.substring(dotIndex + 1);
                }
                sanitized = sanitized.trim();
                if (!sanitized.isEmpty()) {
                    moves.add(sanitized);
                }
            }
            return List.copyOf(moves);
        }

        private static int parseInt(final String value, final int defaultValue) {
            if (value == null || value.isBlank()) {
                return defaultValue;
            }
            try {
                return Integer.parseInt(value.trim());
            } catch (final NumberFormatException ignored) {
                return defaultValue;
            }
        }

        private static double parseDouble(final String value, final double defaultValue) {
            if (value == null || value.isBlank()) {
                return defaultValue;
            }
            try {
                return Double.parseDouble(value.trim());
            } catch (final NumberFormatException ignored) {
                return defaultValue;
            }
        }
    }

    private record OpeningEntry(String name,
                                int numGames,
                                List<String> sanMoves,
                                double whiteWinPercent,
                                double blackWinPercent,
                                double drawPercent) {

        private double winPercent(final Alliance side) {
            return side.isWhite() ? whiteWinPercent : blackWinPercent;
        }

        private double expectedScore(final Alliance side) {
            final double wins = winPercent(side);
            return (wins + 0.5 * drawPercent) / 100.0;
        }
    }
}

