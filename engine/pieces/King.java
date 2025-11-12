package engine.pieces;

import engine.Alliance;
import engine.board.Board;
import engine.board.BoardUtils;
import engine.board.Move;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class King extends Piece {

    private static final int[] CANDIDATE_MOVE_COORDINATES = { -9, -8, -7, -1, 1, 7, 8, 9 };

    public King(final Alliance alliance,
                final int piecePosition,
                final boolean isFirstMove) {
        super(PieceType.KING, alliance, piecePosition, isFirstMove);
    }

    @Override
    public Collection<Move> calculateLegalMoves(final Board board) {
        final List<Move> legalMoves = new ArrayList<>();
        for (final int currentCandidateOffset : CANDIDATE_MOVE_COORDINATES) {
            final int candidateDestination = this.piecePosition + currentCandidateOffset;
            if (!BoardUtils.isValidTileCoordinate(candidateDestination)) {
                continue;
            }
            if (isFirstColumnExclusion(this.piecePosition, currentCandidateOffset) ||
                isEighthColumnExclusion(this.piecePosition, currentCandidateOffset)) {
                continue;
            }
            final var pieceAtDestination = board.getPiece(candidateDestination);
            if (pieceAtDestination.isEmpty()) {
                legalMoves.add(new Move.MajorMove(board, this, candidateDestination));
            } else {
                if (pieceAtDestination.get().getPieceAlliance() != this.pieceAlliance) {
                    legalMoves.add(new Move.MajorAttackMove(board, this, candidateDestination, pieceAtDestination.get()));
                }
            }
        }
        // Castling placeholder
        return legalMoves;
    }

    @Override
    public King movePiece(final Move move) {
        return new King(move.getMovedPiece().getPieceAlliance(), move.getDestinationCoordinate(), false);
    }

    private static boolean isFirstColumnExclusion(final int currentPosition, final int candidateOffset) {
        return BoardUtils.FIRST_COLUMN.get(currentPosition) && (candidateOffset == -9 || candidateOffset == -1 || candidateOffset == 7);
    }

    private static boolean isEighthColumnExclusion(final int currentPosition, final int candidateOffset) {
        return BoardUtils.EIGHTH_COLUMN.get(currentPosition) && (candidateOffset == -7 || candidateOffset == 1 || candidateOffset == 9);
    }
}
