package engine.pieces;

import engine.Alliance;
import engine.board.Board;
import engine.board.BoardUtils;
import engine.board.Move;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Rook extends Piece {

    private static final int[] CANDIDATE_MOVE_VECTOR_COORDINATES = { -8, -1, 1, 8 };

    public Rook(final Alliance alliance,
                final int piecePosition,
                final boolean isFirstMove) {
        super(PieceType.ROOK, alliance, piecePosition, isFirstMove);
    }

    @Override
    public Collection<Move> calculateLegalMoves(final Board board) {
        final List<Move> legalMoves = new ArrayList<>();
        for (final int candidateCoordinateOffset : CANDIDATE_MOVE_VECTOR_COORDINATES) {
            int candidateDestination = this.piecePosition;
            while (true) {
                if (isFirstColumnExclusion(candidateDestination, candidateCoordinateOffset) ||
                    isEighthColumnExclusion(candidateDestination, candidateCoordinateOffset)) {
                    break;
                }
                candidateDestination += candidateCoordinateOffset;
                if (!BoardUtils.isValidTileCoordinate(candidateDestination)) {
                    break;
                }
                final var pieceAtDestination = board.getPiece(candidateDestination);
                if (pieceAtDestination.isEmpty()) {
                    legalMoves.add(new Move.MajorMove(board, this, candidateDestination));
                } else {
                    if (pieceAtDestination.get().getPieceAlliance() != this.pieceAlliance) {
                        legalMoves.add(new Move.MajorAttackMove(board, this, candidateDestination, pieceAtDestination.get()));
                    }
                    break;
                }
            }
        }
        return legalMoves;
    }

    private static boolean isFirstColumnExclusion(final int currentPosition, final int candidateOffset) {
        return BoardUtils.FIRST_COLUMN.get(currentPosition) && (candidateOffset == -1);
    }

    private static boolean isEighthColumnExclusion(final int currentPosition, final int candidateOffset) {
        return BoardUtils.EIGHTH_COLUMN.get(currentPosition) && (candidateOffset == 1);
    }

    @Override
    public Rook movePiece(final Move move) {
        return new Rook(move.getMovedPiece().getPieceAlliance(), move.getDestinationCoordinate(), false);
    }
}
