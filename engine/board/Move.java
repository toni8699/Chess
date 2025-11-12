package engine.board;

import engine.pieces.Piece;

public abstract class Move {

    protected final Board board;
    protected final Piece movedPiece;
    protected final int destinationCoordinate;

    protected Move(final Board board,
                   final Piece movedPiece,
                   final int destinationCoordinate) {
        this.board = board;
        this.movedPiece = movedPiece;
        this.destinationCoordinate = destinationCoordinate;
    }

    public Board getBoard() {
        return board;
    }

    public int getDestinationCoordinate() {
        return destinationCoordinate;
    }

    public Piece getMovedPiece() {
        return movedPiece;
    }

    public boolean isAttack() {
        return false;
    }

    public boolean isCastlingMove() {
        return false;
    }

    public Piece getAttackedPiece() {
        return null;
    }

    public Board execute() {
        throw new UnsupportedOperationException("Move execution not yet implemented");
    }

    public static final class NullMove extends Move {

        public NullMove() {
            super(null, null, -1);
        }

        @Override
        public Board execute() {
            throw new RuntimeException("cannot execute null move!");
        }
    }

    public static Move getNullMove() {
        return new NullMove();
    }
}
