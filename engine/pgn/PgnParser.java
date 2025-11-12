package engine.pgn;

import engine.board.Board;
import engine.board.FENParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PgnParser {

    private static final Pattern TAG_PATTERN = Pattern.compile("\\[(\\w+)\\s+\"(.*?)\"\\]");
    private static final Pattern COMMENT_PATTERN = Pattern.compile("\\{[^}]*}");
    private static final Pattern NAG_PATTERN = Pattern.compile("\\$\\d+");

    private PgnParser() {
    }

    public static Optional<PgnGame> parse(final String pgn) {
        if (pgn == null || pgn.isBlank()) {
            return Optional.empty();
        }

        final Matcher matcher = TAG_PATTERN.matcher(pgn);
        final Map<String, String> tags = new LinkedHashMap<>();
        int lastTagEnd = 0;
        while (matcher.find()) {
            tags.put(matcher.group(1), matcher.group(2));
            lastTagEnd = matcher.end();
        }

        final String movesSection = extractMovesSection(pgn, lastTagEnd);
        final List<String> sanMoves = parseSanTokens(movesSection);

        Board initialBoard = Board.createStandardBoard();
        final String setUp = tags.getOrDefault("SetUp", "0");
        final String fen = tags.get("FEN");
        if (fen != null && ("1".equals(setUp) || setUp.equalsIgnoreCase("true") || "0".equals(setUp))) {
            initialBoard = FENParser.parse(fen).orElse(initialBoard);
        }

        return Optional.of(new PgnGame(initialBoard, List.copyOf(sanMoves), Collections.unmodifiableMap(tags)));
    }

    public record PgnGame(Board initialBoard, List<String> sanMoves, Map<String, String> tags) {
    }

    private static String extractMovesSection(final String pgn, final int startIndex) {
        String moves = pgn.substring(Math.min(startIndex, pgn.length())).trim();
        moves = COMMENT_PATTERN.matcher(moves).replaceAll(" ");
        moves = stripParentheticalVariations(moves);
        moves = moves.replaceAll(";[^\\n]*", " ");
        return moves.trim();
    }

    private static String stripParentheticalVariations(final String text) {
        String stripped = text;
        String previous;
        do {
            previous = stripped;
            stripped = stripped.replaceAll("\\([^()]*\\)", " ");
        } while (!stripped.equals(previous));
        return stripped;
    }

    private static List<String> parseSanTokens(final String movesSection) {
        final List<String> sanMoves = new ArrayList<>();
        if (movesSection.isEmpty()) {
            return sanMoves;
        }
        final String normalised = movesSection.replaceAll("\\s+", " ").trim();
        if (normalised.isEmpty()) {
            return sanMoves;
        }
        for (String token : normalised.split(" ")) {
            if (token.isBlank()) {
                continue;
            }
            token = token.trim();
            if (isGameResult(token)) {
                break;
            }
            token = NAG_PATTERN.matcher(token).replaceAll("");
            token = token.replaceAll("^\\d+\\.{1,3}", "");
            token = token.replaceAll("[!?]+", "");
            token = token.replaceAll("\\u2026", "...");
            token = token.replace('0', 'O');
            token = token.replaceAll("\"", "");
            token = token.trim();
            if (token.isEmpty() || token.equals("...")) {
                continue;
            }
            sanMoves.add(token);
        }
        return sanMoves;
    }

    private static boolean isGameResult(final String token) {
        final String normalised = token.toLowerCase(Locale.ROOT);
        return normalised.equals("1-0") || normalised.equals("0-1")
                || normalised.equals("1/2-1/2") || normalised.equals("*");
    }
}
