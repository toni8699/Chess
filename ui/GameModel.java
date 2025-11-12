package ui;

import engine.board.Board;
import engine.board.BoardUtils;
import engine.board.FENParser;
import engine.board.Move;
import engine.board.MoveTransition;
import engine.player.Player;
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
    private final List<String> moveHistory;

    public GameModel() {
        this.currentBoard = Board.createStandardBoard();
        this.history = new ArrayDeque<>();
        this.moveHistory = new ArrayList<>();
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

        final MoveTransition transition = currentBoard.getCurrentPlayer().makeMove(candidateMove.get());
        if (!transition.getMoveStatus().isDone()) {
            return false;
        }

        history.push(currentBoard);
        currentBoard = transition.getToBoard();
        moveHistory.add(describeMove(candidateMove.get(), transition.getToBoard().getCurrentPlayer().getOpponent()));
        return true;
    }

    public boolean undo() {
        if (history.isEmpty()) {
            return false;
        }
        currentBoard = history.pop();
        if (!moveHistory.isEmpty()) {
            moveHistory.remove(moveHistory.size() - 1);
        }
        return true;
    }

    public void reset() {
        currentBoard = Board.createStandardBoard();
        history.clear();
        moveHistory.clear();
    }

    public List<String> getMoveHistory() {
        return List.copyOf(moveHistory);
    }

    public boolean loadPositionFromFen(final String fen) {
        final Optional<Board> parsed = FENParser.parse(fen);
        if (parsed.isEmpty()) {
            return false;
        }
        currentBoard = parsed.get();
        history.clear();
        moveHistory.clear();
        return true;
    }

    public boolean loadGameFromPgn(final String pgn) {
        final Optional<PgnParser.PgnGame> parsed = PgnParser.parse(pgn);
        if (parsed.isEmpty()) {
            return false;
        }

        Board workingBoard = parsed.get().initialBoard();
        final Deque<Board> workingHistory = new ArrayDeque<>();
        final List<String> workingMoveHistory = new ArrayList<>();

        for (final String san : parsed.get().sanMoves()) {
            final Optional<Move> candidate = findMoveForSan(workingBoard, san);
            if (candidate.isEmpty()) {
                return false;
            }
            workingHistory.push(workingBoard);
            final MoveTransition transition = workingBoard.getCurrentPlayer().makeMove(candidate.get());
            if (!transition.getMoveStatus().isDone()) {
                return false;
            }
            workingBoard = transition.getToBoard();
            workingMoveHistory.add(describeMove(candidate.get(), workingBoard.getCurrentPlayer().getOpponent()));
        }

        currentBoard = workingBoard;
        history.clear();
        history.addAll(workingHistory);
        moveHistory.clear();
        moveHistory.addAll(workingMoveHistory);
        return true;
    }

    private String describeMove(final Move move, final Player movingPlayer) {
        final String pieceSymbol = move.getMovedPiece().getPieceType().toString();
        final String from = BoardUtils.INSTANCE.getPositionAtCoordinate(move.getMovedPiece().getPiecePosition());
        final String to = BoardUtils.INSTANCE.getPositionAtCoordinate(move.getDestinationCoordinate());
        final boolean capture = move.isAttack();
        final boolean promotion = move instanceof Move.PawnPromotion;
        final StringBuilder builder = new StringBuilder();
        builder.append(movingPlayer.getAlliance().toString().charAt(0)).append(": ");
        if (!pieceSymbol.equals("P")) {
            builder.append(pieceSymbol);
        }
        builder.append(from);
        builder.append(capture ? "x" : "-");
        builder.append(to);
        if (promotion) {
            final Move.PawnPromotion promotionMove = (Move.PawnPromotion) move;
            builder.append("=").append(promotionMove.getPromotionPiece().getPieceType().toString());
        }
        return builder.toString();
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
