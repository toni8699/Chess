package engine.board;

import engine.pieces.King;
import engine.pieces.Pawn;
import engine.pieces.Piece;
import engine.pieces.Rook;

import java.util.Objects;

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
        final Board.Builder builder = new Board.Builder();
        for (final Piece piece : board.getActivePieces()) {
            if (!piece.equals(movedPiece)) {
                builder.setPiece(piece);
            }
        }
        builder.setPiece(movedPiece.movePiece(this));
        builder.setMoveMaker(board.getCurrentPlayer().getOppositeAlliance());
        return builder.build();
    }

    @Override
    public int hashCode() {
        return Objects.hash(board, movedPiece, destinationCoordinate);
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Move)) {
            return false;
        }
        final Move move = (Move) other;
        return destinationCoordinate == move.destinationCoordinate
                && movedPiece.equals(move.movedPiece)
                && board.equals(move.board);
    }

    public static class MajorMove extends Move {
        public MajorMove(final Board board,
                         final Piece movedPiece,
                         final int destinationCoordinate) {
            super(board, movedPiece, destinationCoordinate);
        }
    }

    public abstract static class AttackMove extends Move {
        private final Piece attackedPiece;

        protected AttackMove(final Board board,
                             final Piece movedPiece,
                             final int destinationCoordinate,
                             final Piece attackedPiece) {
            super(board, movedPiece, destinationCoordinate);
            this.attackedPiece = attackedPiece;
        }

        @Override
        public boolean isAttack() {
            return true;
        }

        @Override
        public Piece getAttackedPiece() {
            return attackedPiece;
        }

        @Override
        public Board execute() {
            final Board.Builder builder = new Board.Builder();
            for (final Piece piece : board.getActivePieces()) {
                if (!piece.equals(movedPiece) && !piece.equals(attackedPiece)) {
                    builder.setPiece(piece);
                }
            }
            builder.setPiece(movedPiece.movePiece(this));
            builder.setMoveMaker(board.getCurrentPlayer().getOppositeAlliance());
            return builder.build();
        }
    }

    public static class MajorAttackMove extends AttackMove {
        public MajorAttackMove(final Board board,
                               final Piece movedPiece,
                               final int destinationCoordinate,
                               final Piece attackedPiece) {
            super(board, movedPiece, destinationCoordinate, attackedPiece);
        }
    }

    public static class PawnMove extends Move {
        public PawnMove(final Board board,
                        final Piece movedPiece,
                        final int destinationCoordinate) {
            super(board, movedPiece, destinationCoordinate);
        }

        @Override
        public Board execute() {
            final Board.Builder builder = new Board.Builder();
            for (final Piece piece : board.getActivePieces()) {
                if (!piece.equals(movedPiece)) {
                    builder.setPiece(piece);
                }
            }
            final Pawn movedPawn = (Pawn) movedPiece.movePiece(this);
            builder.setPiece(movedPawn);
            builder.setMoveMaker(board.getCurrentPlayer().getOppositeAlliance());
            builder.setEnPassantPawn(movedPawn.hasMovedTwoSquares(this) ? movedPawn : null);
            return builder.build();
        }
    }

    public static class PawnAttackMove extends AttackMove {
        public PawnAttackMove(final Board board,
                              final Piece movedPiece,
                              final int destinationCoordinate,
                              final Piece attackedPiece) {
            super(board, movedPiece, destinationCoordinate, attackedPiece);
        }
    }

    public static class PawnEnPassantAttackMove extends PawnAttackMove {
        public PawnEnPassantAttackMove(final Board board,
                                       final Piece movedPiece,
                                       final int destinationCoordinate,
                                       final Piece attackedPiece) {
            super(board, movedPiece, destinationCoordinate, attackedPiece);
        }

        @Override
        public Board execute() {
            final Board.Builder builder = new Board.Builder();
            for (final Piece piece : board.getActivePieces()) {
                if (!piece.equals(movedPiece) && !piece.equals(getAttackedPiece())) {
                    builder.setPiece(piece);
                }
            }
            builder.setPiece(movedPiece.movePiece(this));
            builder.setMoveMaker(board.getCurrentPlayer().getOppositeAlliance());
            return builder.build();
        }
    }

    public static class PawnJump extends Move {
        public PawnJump(final Board board,
                        final Pawn movedPiece,
                        final int destinationCoordinate) {
            super(board, movedPiece, destinationCoordinate);
        }

        @Override
        public Board execute() {
            final Board.Builder builder = new Board.Builder();
            for (final Piece piece : board.getActivePieces()) {
                if (!piece.equals(movedPiece)) {
                    builder.setPiece(piece);
                }
            }
            final Pawn movedPawn = (Pawn) movedPiece.movePiece(this);
            builder.setPiece(movedPawn);
            builder.setEnPassantPawn(movedPawn);
            builder.setMoveMaker(board.getCurrentPlayer().getOppositeAlliance());
            return builder.build();
        }
    }

    public abstract static class CastleMove extends Move {
        protected final Rook castleRook;
        protected final int castleRookDestination;

        protected CastleMove(final Board board,
                             final King movedPiece,
                             final int destinationCoordinate,
                             final Rook castleRook,
                             final int castleRookDestination) {
            super(board, movedPiece, destinationCoordinate);
            this.castleRook = castleRook;
            this.castleRookDestination = castleRookDestination;
        }

        @Override
        public boolean isCastlingMove() {
            return true;
        }

        @Override
        public Board execute() {
            final Board.Builder builder = new Board.Builder();
            for (final Piece piece : board.getActivePieces()) {
                if (!piece.equals(movedPiece) && !piece.equals(castleRook)) {
                    builder.setPiece(piece);
                }
            }
            builder.setPiece(movedPiece.movePiece(this));
            builder.setPiece(new Rook(castleRook.getPieceAlliance(), castleRookDestination, false));
            builder.setMoveMaker(board.getCurrentPlayer().getOppositeAlliance());
            return builder.build();
        }
    }

    public static class KingSideCastleMove extends CastleMove {
        public KingSideCastleMove(final Board board,
                                  final King movedPiece,
                                  final int destinationCoordinate,
                                  final Rook castleRook,
                                  final int castleRookDestination) {
            super(board, movedPiece, destinationCoordinate, castleRook, castleRookDestination);
        }
    }

    public static class QueenSideCastleMove extends CastleMove {
        public QueenSideCastleMove(final Board board,
                                   final King movedPiece,
                                   final int destinationCoordinate,
                                   final Rook castleRook,
                                   final int castleRookDestination) {
            super(board, movedPiece, destinationCoordinate, castleRook, castleRookDestination);
        }
    }

    public static class PawnPromotion extends Move {
        private final Move decoratedMove;
        private final Piece promotionPiece;

        public PawnPromotion(final Move decoratedMove,
                             final Piece promotionPiece) {
            super(decoratedMove.getBoard(), decoratedMove.getMovedPiece(), decoratedMove.getDestinationCoordinate());
            this.decoratedMove = decoratedMove;
            this.promotionPiece = promotionPiece;
        }

        public Move getDecoratedMove() {
            return decoratedMove;
        }

        public Piece getPromotionPiece() {
            return promotionPiece;
        }

        @Override
        public Board execute() {
            final Board promotedBoard = decoratedMove.execute();
            final Board.Builder builder = new Board.Builder();
            for (final Piece piece : promotedBoard.getActivePieces()) {
                if (!piece.equals(decoratedMove.getMovedPiece())) {
                    builder.setPiece(piece);
                }
            }
            builder.setPiece(promotionPiece);
            builder.setMoveMaker(promotedBoard.getCurrentPlayer());
            return builder.build();
        }
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
