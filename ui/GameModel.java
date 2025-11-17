package ui;

import engine.ai.BoardEvaluator;
import engine.ai.MiniMax;
import engine.ai.MoveScore;
import engine.ai.SimpleBoardEvaluator;
import engine.board.Board;
import engine.board.FENParser;
import engine.board.Move;
import engine.board.MoveTransition;
import engine.pieces.Piece;
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
    private final BoardEvaluator boardEvaluator;
    private int aiDepth = 3;
    private final List<Piece.PieceType> capturedWhite = new ArrayList<>();
    private final List<Piece.PieceType> capturedBlack = new ArrayList<>();
    private final Deque<List<Piece.PieceType>> capturedWhiteHistory = new ArrayDeque<>();
    private final Deque<List<Piece.PieceType>> capturedBlackHistory = new ArrayDeque<>();

    public GameModel() {
        this.currentBoard = Board.createStandardBoard();
        this.history = new ArrayDeque<>();
        this.sanHistory = new ArrayList<>();
        this.boardEvaluator = new SimpleBoardEvaluator();
    }

    public Board getBoard() {
        return currentBoard;
    }

    public Collection<Move> getLegalMovesFrom(final int sourceCoordinate) {
        return currentBoard.getCurrentPlayer().getLegalMoves().stream()
                .filter(move -> move.getMovedPiece().getPiecePosition() == sourceCoordinate)
                .filter(move -> currentBoard.getCurrentPlayer().makeMove(move).getMoveStatus().isDone())
                .collect(Collectors.toUnmodifiableList());
    }

    public MoveAttemptResult makeMove(final int sourceCoordinate, final int destinationCoordinate) {
        return makeMove(sourceCoordinate, destinationCoordinate, null);
    }

    public MoveAttemptResult makeMove(final int sourceCoordinate,
                                      final int destinationCoordinate,
                                      final Piece.PieceType promotionChoice) {
        final var currentPlayer = currentBoard.getCurrentPlayer();
        final List<Move> candidateMoves = currentPlayer.getLegalMoves().stream()
                .filter(move -> move.getMovedPiece().getPiecePosition() == sourceCoordinate
                        && move.getDestinationCoordinate() == destinationCoordinate)
                .collect(Collectors.toList());
        if (candidateMoves.isEmpty()) {
            return MoveAttemptResult.illegal();
        }

        Move moveToExecute;
        final List<Move> promotionMoves = candidateMoves.stream()
                .filter(move -> move instanceof Move.PawnPromotion)
                .collect(Collectors.toList());
        if (!promotionMoves.isEmpty()) {
            if (promotionChoice == null) {
                final List<Piece.PieceType> options = promotionMoves.stream()
                        .map(move -> ((Move.PawnPromotion) move).getPromotionPiece().getPieceType())
                        .distinct()
                        .collect(Collectors.toList());
                return MoveAttemptResult.promotionRequired(options);
            }
            final Optional<Move> chosen = promotionMoves.stream()
                    .filter(move -> ((Move.PawnPromotion) move).getPromotionPiece().getPieceType() == promotionChoice)
                    .findFirst();
            if (chosen.isEmpty()) {
                return MoveAttemptResult.illegal();
            }
            moveToExecute = chosen.get();
        } else {
            moveToExecute = candidateMoves.get(0);
        }

        final MoveTransition transition = currentPlayer.makeMove(moveToExecute);
        if (!transition.getMoveStatus().isDone()) {
            return MoveAttemptResult.illegal();
        }

        capturedWhiteHistory.push(new ArrayList<>(capturedWhite));
        capturedBlackHistory.push(new ArrayList<>(capturedBlack));

        final Board previousBoard = currentBoard;
        history.push(previousBoard);
        currentBoard = transition.getToBoard();
        sanHistory.add(SanGenerator.generateSan(previousBoard, moveToExecute));
        updateCapturedPieces(moveToExecute, capturedWhite, capturedBlack);
        return MoveAttemptResult.done();
    }

    public MoveAttemptResult makeMove(final Move move) {
        if (move == null) {
            return MoveAttemptResult.illegal();
        }
        Piece.PieceType promotionChoice = null;
        if (move instanceof Move.PawnPromotion pawnPromotion) {
            promotionChoice = pawnPromotion.getPromotionPiece().getPieceType();
        }
        return makeMove(move.getMovedPiece().getPiecePosition(),
                move.getDestinationCoordinate(),
                promotionChoice);
    }

    public boolean undo() {
        if (history.isEmpty()) {
            return false;
        }
        currentBoard = history.pop();
        restoreCapturedHistory();
        if (!sanHistory.isEmpty()) {
            sanHistory.remove(sanHistory.size() - 1);
        }
        return true;
    }

    public CapturedPieces getCapturedPieces() {
        return new CapturedPieces(List.copyOf(capturedWhite), List.copyOf(capturedBlack));
    }

    public void reset() {
        currentBoard = Board.createStandardBoard();
        history.clear();
        sanHistory.clear();
        capturedWhite.clear();
        capturedBlack.clear();
        capturedWhiteHistory.clear();
        capturedBlackHistory.clear();
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
        capturedWhite.clear();
        capturedBlack.clear();
        capturedWhiteHistory.clear();
        capturedBlackHistory.clear();
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
        final List<Piece.PieceType> tempCapturedWhite = new ArrayList<>();
        final List<Piece.PieceType> tempCapturedBlack = new ArrayList<>();
        final Deque<List<Piece.PieceType>> tempCapturedWhiteHistory = new ArrayDeque<>();
        final Deque<List<Piece.PieceType>> tempCapturedBlackHistory = new ArrayDeque<>();

        for (final String san : parsed.get().sanMoves()) {
            final Optional<Move> candidate = findMoveForSan(workingBoard, san);
            if (candidate.isEmpty()) {
                return false;
            }
            final String canonicalSan = SanGenerator.generateSan(workingBoard, candidate.get());
            workingHistory.push(workingBoard);
            tempCapturedWhiteHistory.push(new ArrayList<>(tempCapturedWhite));
            tempCapturedBlackHistory.push(new ArrayList<>(tempCapturedBlack));
            final MoveTransition transition = workingBoard.getCurrentPlayer().makeMove(candidate.get());
            if (!transition.getMoveStatus().isDone()) {
                return false;
            }
            updateCapturedPieces(candidate.get(), tempCapturedWhite, tempCapturedBlack);
            workingBoard = transition.getToBoard();
            workingSanHistory.add(canonicalSan);
        }

        currentBoard = workingBoard;
        history.clear();
        history.addAll(workingHistory);
        sanHistory.clear();
        sanHistory.addAll(workingSanHistory);
        capturedWhite.clear();
        capturedWhite.addAll(tempCapturedWhite);
        capturedBlack.clear();
        capturedBlack.addAll(tempCapturedBlack);
        capturedWhiteHistory.clear();
        capturedWhiteHistory.addAll(tempCapturedWhiteHistory);
        capturedBlackHistory.clear();
        capturedBlackHistory.addAll(tempCapturedBlackHistory);
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

    public void setAiDepth(final int depth) {
        this.aiDepth = Math.max(1, depth);
    }

    public int getAiDepth() {
        return aiDepth;
    }

    public List<ScoredMove> getBestMoves(final int maxMoves) {
        final MiniMax miniMax = new MiniMax(boardEvaluator);
        final List<MoveScore> scores = miniMax.getBestMoves(currentBoard, aiDepth, maxMoves);
        final List<ScoredMove> result = scores.stream()
                .map(moveScore -> {
                    final String san = SanGenerator.generateSan(currentBoard, moveScore.move());
                    return new ScoredMove(san, moveScore.score(), moveScore.move());
                })
                .collect(Collectors.toList());
        return List.copyOf(result);
    }

    public enum MoveAttemptStatus {
        DONE,
        ILLEGAL,
        PROMOTION_REQUIRED
    }

    public record MoveAttemptResult(MoveAttemptStatus status, List<Piece.PieceType> promotionOptions) {
        public static MoveAttemptResult done() {
            return new MoveAttemptResult(MoveAttemptStatus.DONE, List.of());
        }

        public static MoveAttemptResult illegal() {
            return new MoveAttemptResult(MoveAttemptStatus.ILLEGAL, List.of());
        }

        public static MoveAttemptResult promotionRequired(final List<Piece.PieceType> options) {
            return new MoveAttemptResult(MoveAttemptStatus.PROMOTION_REQUIRED, List.copyOf(options));
        }
    }

    public record CapturedPieces(List<Piece.PieceType> whiteCaptured,
                                 List<Piece.PieceType> blackCaptured) {
    }

    private void restoreCapturedHistory() {
        if (!capturedWhiteHistory.isEmpty()) {
            capturedWhite.clear();
            capturedWhite.addAll(capturedWhiteHistory.pop());
        } else {
            capturedWhite.clear();
        }
        if (!capturedBlackHistory.isEmpty()) {
            capturedBlack.clear();
            capturedBlack.addAll(capturedBlackHistory.pop());
        } else {
            capturedBlack.clear();
        }
    }

    private void updateCapturedPieces(final Move move,
                                      final List<Piece.PieceType> whiteCaptured,
                                      final List<Piece.PieceType> blackCaptured) {
        if (!move.isAttack()) {
            return;
        }
        final Piece attackedPiece = move.getAttackedPiece();
        if (attackedPiece == null) {
            return;
        }
        if (attackedPiece.getPieceAlliance().isWhite()) {
            whiteCaptured.add(attackedPiece.getPieceType());
        } else {
            blackCaptured.add(attackedPiece.getPieceType());
        }
    }

    public record ScoredMove(String san, int score, Move move) {
    }
}
