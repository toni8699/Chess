package engine.pgn;

import engine.board.Board;
import engine.board.BoardUtils;
import engine.board.Move;
import engine.pieces.Piece;
import engine.player.Player;

import java.util.Collection;
import java.util.stream.Collectors;

public final class SanGenerator {

    private SanGenerator() {
    }

    public static String generateSan(final Board board, final Move move) {
        if (move instanceof Move.KingSideCastleMove) {
            return appendCheckSymbols("O-O", move);
        }
        if (move instanceof Move.QueenSideCastleMove) {
            return appendCheckSymbols("O-O-O", move);
        }

        Move baseMove = move;
        Piece promotionPiece = null;
        if (move instanceof Move.PawnPromotion promotion) {
            baseMove = promotion.getDecoratedMove();
            promotionPiece = promotion.getPromotionPiece();
        }

        final Piece movedPiece = baseMove.getMovedPiece();
        final StringBuilder san = new StringBuilder();

        if (movedPiece.getPieceType() != Piece.PieceType.PAWN) {
            san.append(movedPiece.getPieceType());
            san.append(calculateDisambiguation(board, move, baseMove));
        } else if (isCapture(baseMove)) {
            final String fromSquare = BoardUtils.INSTANCE.getPositionAtCoordinate(movedPiece.getPiecePosition());
            san.append(fromSquare.charAt(0));
        }

        if (isCapture(baseMove)) {
            san.append('x');
        }

        san.append(BoardUtils.INSTANCE.getPositionAtCoordinate(baseMove.getDestinationCoordinate()));

        if (promotionPiece != null) {
            san.append('=').append(promotionPiece.getPieceType());
        }

        return appendCheckSymbols(san.toString(), move);
    }

    private static boolean isCapture(final Move move) {
        if (move instanceof Move.PawnPromotion promotion) {
            return promotion.getDecoratedMove().isAttack();
        }
        return move.isAttack();
    }

    private static String calculateDisambiguation(final Board board,
                                                   final Move originalMove,
                                                   final Move baseMove) {
        final Piece movedPiece = baseMove.getMovedPiece();
        final Collection<Move> candidates = board.getCurrentPlayer().getLegalMoves().stream()
                .filter(other -> other != originalMove)
                .map(SanGenerator::extractBaseMove)
                .filter(other -> other.getMovedPiece().getPieceType() == movedPiece.getPieceType())
                .filter(other -> other.getDestinationCoordinate() == baseMove.getDestinationCoordinate())
                .collect(Collectors.toList());

        if (candidates.isEmpty()) {
            return "";
        }

        final String fromSquare = BoardUtils.INSTANCE.getPositionAtCoordinate(movedPiece.getPiecePosition());
        final char file = fromSquare.charAt(0);
        final char rank = fromSquare.charAt(1);

        boolean sameFile = false;
        boolean sameRank = false;

        for (final Move candidate : candidates) {
            final String candidateSquare = BoardUtils.INSTANCE.getPositionAtCoordinate(candidate.getMovedPiece().getPiecePosition());
            if (candidateSquare.charAt(0) == file) {
                sameFile = true;
            }
            if (candidateSquare.charAt(1) == rank) {
                sameRank = true;
            }
        }

        if (!sameFile) {
            return String.valueOf(file);
        }
        if (!sameRank) {
            return String.valueOf(rank);
        }
        return "" + file + rank;
    }

    private static Move extractBaseMove(final Move move) {
        if (move instanceof Move.PawnPromotion promotion) {
            return promotion.getDecoratedMove();
        }
        return move;
    }

    private static String appendCheckSymbols(final String san, final Move move) {
        final Board nextBoard = move.execute();
        final Player nextPlayer = nextBoard.getCurrentPlayer();
        if (nextPlayer.isInCheckMate()) {
            return san + '#';
        }
        if (nextPlayer.isInCheck()) {
            return san + '+';
        }
        return san;
    }
}
