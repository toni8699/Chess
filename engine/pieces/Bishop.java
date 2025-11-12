package engine.pieces;

import engine.Alliance;
import engine.board.Board;
import engine.board.BoardUtils;
import engine.board.Move;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Bishop extends Piece {

    private static final int[] CANDIDATE_MOVE_VECTOR_COORDINATES = { -9, -7, 7, 9 };

    public Bishop(final Alliance alliance,
                  final int piecePosition,
                  final boolean isFirstMove) {
        super(PieceType.BISHOP, alliance, piecePosition, isFirstMove);
    }

    @Override
    public Collection<Move> calculateLegalMoves(final Board board) {
        final List<Move> legalMoves = new ArrayList<>();
        for (final int candidateCoordinateOffset : CANDIDATE_MOVE_VECTOR_COORDINATES) {
            int candidateDestination = this.piecePosition;
            while (BoardUtils.isValidTileCoordinate(candidateDestination)) {
                candidateDestination += candidateCoordinateOffset;
                if (!BoardUtils.isValidTileCoordinate(candidateDestination)) {
                    break;
                }
                if (isFirstColumnExclusion(candidateDestination, candidateCoordinateOffset) ||
                    isEighthColumnExclusion(candidateDestination, candidateCoordinateOffset)) {
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
        return BoardUtils.FIRST_COLUMN.get(currentPosition) && (candidateOffset == -9 || candidateOffset == 7);
    }

    private static boolean isEighthColumnExclusion(final int currentPosition, final int candidateOffset) {
        return BoardUtils.EIGHTH_COLUMN.get(currentPosition) && (candidateOffset == -7 || candidateOffset == 9);
    }

    @Override
    public Bishop movePiece(final Move move) {
        return new Bishop(move.getMovedPiece().getPieceAlliance(), move.getDestinationCoordinate(), false);
    }
}
