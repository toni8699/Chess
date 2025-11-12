package engine.pieces;

import engine.Alliance;
import engine.board.Board;
import engine.board.Move;

import java.util.Collection;

public abstract class Piece {

    protected final PieceType pieceType;
    protected final Alliance pieceAlliance;
    protected final int piecePosition;
    protected final boolean isFirstMove;
    private final int cachedHashCode;

    protected Piece(final PieceType pieceType,
                    final Alliance pieceAlliance,
                    final int piecePosition,
                    final boolean isFirstMove) {
        this.pieceType = pieceType;
        this.pieceAlliance = pieceAlliance;
        this.piecePosition = piecePosition;
        this.isFirstMove = isFirstMove;
        this.cachedHashCode = computeHashCode();
    }

    private int computeHashCode() {
        int result = pieceType.hashCode();
        result = 31 * result + pieceAlliance.hashCode();
        result = 31 * result + piecePosition;
        result = 31 * result + (isFirstMove ? 1 : 0);
        return result;
    }

    public PieceType getPieceType() {
        return pieceType;
    }

    public Alliance getPieceAlliance() {
        return pieceAlliance;
    }

    public int getPiecePosition() {
        return piecePosition;
    }

    public boolean isFirstMove() {
        return isFirstMove;
    }

    public abstract Collection<Move> calculateLegalMoves(final Board board);

    public abstract Piece movePiece(final Move move);

    @Override
    public int hashCode() {
        return cachedHashCode;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Piece)) {
            return false;
        }
        final Piece piece = (Piece) other;
        return piecePosition == piece.piecePosition && pieceType == piece.pieceType && pieceAlliance == piece.pieceAlliance && isFirstMove == piece.isFirstMove;
    }

    public enum PieceType {
        PAWN("P"),
        KNIGHT("N"),
        BISHOP("B"),
        ROOK("R"),
        QUEEN("Q"),
        KING("K");

        private final String representation;

        PieceType(final String representation) {
            this.representation = representation;
        }

        @Override
        public String toString() {
            return this.representation;
        }
    }
}
