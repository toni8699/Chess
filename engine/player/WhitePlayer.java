package engine.player;

import engine.Alliance;
import engine.board.Board;
import engine.board.BoardUtils;
import engine.board.Move;
import engine.pieces.Piece;
import engine.pieces.Rook;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class WhitePlayer extends Player {

    public WhitePlayer(final Board board,
                       final Collection<Move> whiteStandardLegalMoves,
                       final Collection<Move> blackStandardLegalMoves) {
        super(board, whiteStandardLegalMoves, blackStandardLegalMoves);
    }

    @Override
    public Collection<Piece> getActivePieces() {
        return board.getWhitePieces();
    }

    @Override
    public Alliance getAlliance() {
        return Alliance.WHITE;
    }

    @Override
    public Player getOpponent() {
        return board.getBlackPlayer();
    }

    @Override
    protected Collection<Move> calculateKingCastles(final Collection<Move> opponentMoves) {
        final List<Move> kingCastles = new ArrayList<>();
        if (!playerKing.isFirstMove() || isInCheck()) {
            return kingCastles;
        }

        final int f1 = BoardUtils.INSTANCE.getCoordinateAtPosition("f1");
        final int g1 = BoardUtils.INSTANCE.getCoordinateAtPosition("g1");
        final int h1 = BoardUtils.INSTANCE.getCoordinateAtPosition("h1");
        if (board.getPiece(f1).isEmpty()
                && board.getPiece(g1).isEmpty()) {
            final Optional<Piece> rook = board.getPiece(h1);
            if (rook.isPresent()
                    && rook.get().getPieceAlliance().isWhite()
                    && rook.get().getPieceType() == Piece.PieceType.ROOK
                    && rook.get().isFirstMove()
                    && calculateAttacksOnTile(f1, opponentMoves).isEmpty()
                    && calculateAttacksOnTile(g1, opponentMoves).isEmpty()) {
                kingCastles.add(new Move.KingSideCastleMove(board, playerKing, g1, (Rook) rook.get(), f1));
            }
        }

        final int d1 = BoardUtils.INSTANCE.getCoordinateAtPosition("d1");
        final int c1 = BoardUtils.INSTANCE.getCoordinateAtPosition("c1");
        final int b1 = BoardUtils.INSTANCE.getCoordinateAtPosition("b1");
        final int a1 = BoardUtils.INSTANCE.getCoordinateAtPosition("a1");
        if (board.getPiece(d1).isEmpty()
                && board.getPiece(c1).isEmpty()
                && board.getPiece(b1).isEmpty()) {
            final Optional<Piece> rook = board.getPiece(a1);
            if (rook.isPresent()
                    && rook.get().getPieceAlliance().isWhite()
                    && rook.get().getPieceType() == Piece.PieceType.ROOK
                    && rook.get().isFirstMove()
                    && calculateAttacksOnTile(d1, opponentMoves).isEmpty()
                    && calculateAttacksOnTile(c1, opponentMoves).isEmpty()) {
                kingCastles.add(new Move.QueenSideCastleMove(board, playerKing, c1, (Rook) rook.get(), d1));
            }
        }
        return kingCastles;
    }
}
