package engine.ai;

import engine.board.Board;
import engine.pieces.Piece;
import engine.player.Player;

public class SimpleBoardEvaluator implements BoardEvaluator {

    private static final int CHECKMATE_SCORE = 100_000;
    private static final int CHECK_BONUS = 50;
    private static final int MOBILITY_MULTIPLIER = 5;

    @Override
    public int evaluate(final Board board, final int depth) {
        return scorePlayer(board, board.getWhitePlayer(), depth)
                - scorePlayer(board, board.getBlackPlayer(), depth);
    }

    private int scorePlayer(final Board board, final Player player, final int depth) {
        if (player.isInCheckMate()) {
            return -CHECKMATE_SCORE + depth;
        }
        if (player.getOpponent().isInCheckMate()) {
            return CHECKMATE_SCORE - depth;
        }
        int score = pieceValue(player) + mobility(player);
        if (player.isInCheck()) {
            score -= CHECK_BONUS;
        }
        return score;
    }

    private int mobility(final Player player) {
        return player.getLegalMoves().size() * MOBILITY_MULTIPLIER;
    }

    private int pieceValue(final Player player) {
        int value = 0;
        for (final Piece piece : player.getActivePieces()) {
            value += pieceValue(piece);
        }
        return value;
    }

    private int pieceValue(final Piece piece) {
        return switch (piece.getPieceType()) {
            case KING -> 0;
            case QUEEN -> 900;
            case ROOK -> 500;
            case BISHOP -> 330;
            case KNIGHT -> 320;
            case PAWN -> 100;
        };
    }
}
