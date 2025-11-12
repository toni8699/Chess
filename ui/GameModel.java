package ui;

import engine.board.Board;
import engine.board.FENParser;
import engine.board.Move;
import engine.board.MoveTransition;
import engine.pgn.PgnParser;
import engine.pgn.SanGenerator;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

public class GameModel {

    private Board currentBoard;
    private final Deque<Board> history;
    private final List<String> sanHistory;

    public GameModel() {
        this.currentBoard = Board.createStandardBoard();
        this.history = new ArrayDeque<>();
        this.sanHistory = new ArrayList<>();
    }

    public Board getBoard() {
        return currentBoard;
    }

    public Collection<Move> getLegalMovesFrom(final int sourceCoordinate) {
        return currentBoard.getCurrentPlayer().getLegalMoves().stream()
                .filter(move -> move.getMovedPiece().getPiecePosition() == sourceCoordinate)
                .collect(Collectors.toUnmodifiableList());
    }

    public boolean makeMove(final int sourceCoordinate, final int destinationCoordinate) {
        final Optional<Move> candidateMove = currentBoard.getCurrentPlayer().getLegalMoves().stream()
                .filter(move -> move.getMovedPiece().getPiecePosition() == sourceCoordinate
                        && move.getDestinationCoordinate() == destinationCoordinate)
                .findFirst();
        if (candidateMove.isEmpty()) {
            return false;
        }

        final String san = SanGenerator.generateSan(currentBoard, candidateMove.get());
        final MoveTransition transition = currentBoard.getCurrentPlayer().makeMove(candidateMove.get());
        if (!transition.getMoveStatus().isDone()) {
            return false;
        }

        history.push(currentBoard);
        currentBoard = transition.getToBoard();
        sanHistory.add(san);
        return true;
    }

    public boolean undo() {
        if (history.isEmpty()) {
            return false;
        }
        currentBoard = history.pop();
        if (!sanHistory.isEmpty()) {
            sanHistory.remove(sanHistory.size() - 1);
        }
        return true;
    }

    public void reset() {
        currentBoard = Board.createStandardBoard();
        history.clear();
        sanHistory.clear();
    }

    public List<String> getMoveHistory() {
        if (sanHistory.isEmpty()) {
            return List.of();
        }
        final List<String> formatted = new ArrayList<>();
        for (int i = 0; i < sanHistory.size(); i += 2) {
            final int moveNumber = (i / 2) + 1;
            final StringBuilder row = new StringBuilder();
            row.append(moveNumber).append(". ").append(sanHistory.get(i));
            if (i + 1 < sanHistory.size()) {
                row.append(' ').append(sanHistory.get(i + 1));
            }
            formatted.add(row.toString());
        }
        return List.copyOf(formatted);
    }

    public boolean loadPositionFromFen(final String fen) {
        final Optional<Board> parsed = FENParser.parse(fen);
        if (parsed.isEmpty()) {
            return false;
        }
        currentBoard = parsed.get();
        history.clear();
        sanHistory.clear();
        return true;
    }

    public boolean loadGameFromPgn(final String pgn) {
        final Optional<PgnParser.PgnGame> parsed = PgnParser.parse(pgn);
        if (parsed.isEmpty()) {
            return false;
        }

        Board workingBoard = parsed.get().initialBoard();
        final Deque<Board> workingHistory = new ArrayDeque<>();
        final List<String> workingSanHistory = new ArrayList<>();

        for (final String san : parsed.get().sanMoves()) {
            final Optional<Move> candidate = findMoveForSan(workingBoard, san);
            if (candidate.isEmpty()) {
                return false;
            }
            final String canonicalSan = SanGenerator.generateSan(workingBoard, candidate.get());
            workingHistory.push(workingBoard);
            final MoveTransition transition = workingBoard.getCurrentPlayer().makeMove(candidate.get());
            if (!transition.getMoveStatus().isDone()) {
                return false;
            }
            workingBoard = transition.getToBoard();
            workingSanHistory.add(canonicalSan);
        }

        currentBoard = workingBoard;
        history.clear();
        history.addAll(workingHistory);
        sanHistory.clear();
        sanHistory.addAll(workingSanHistory);
        return true;
    }

    private Optional<Move> findMoveForSan(final Board board, final String san) {
        final String target = normalizeSan(san);
        for (final Move move : board.getCurrentPlayer().getLegalMoves()) {
            final String candidate = normalizeSan(SanGenerator.generateSan(board, move));
            if (target.equalsIgnoreCase(candidate)) {
                return Optional.of(move);
            }
        }
        return Optional.empty();
    }

    private String normalizeSan(final String san) {
        String sanitized = san == null ? "" : san.replaceAll("\\s+", "");
        sanitized = sanitized.replace('0', 'O');
        sanitized = sanitized.replaceAll("[!?]+", "");
        sanitized = sanitized.replaceAll("\\$\\d+", "");
        return sanitized.toLowerCase(Locale.ROOT);
    }
}
