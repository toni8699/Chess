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

public class BlackPlayer extends Player {

    public BlackPlayer(final Board board,
                       final Collection<Move> blackStandardLegalMoves,
                       final Collection<Move> whiteStandardLegalMoves) {
        super(board, blackStandardLegalMoves, whiteStandardLegalMoves);
    }

    @Override
    public Collection<Piece> getActivePieces() {
        return board.getBlackPieces();
    }

    @Override
    public Alliance getAlliance() {
        return Alliance.BLACK;
    }

    @Override
    public Player getOpponent() {
        return board.getWhitePlayer();
    }

    @Override
    protected Collection<Move> calculateKingCastles(final Collection<Move> opponentMoves) {
        final List<Move> kingCastles = new ArrayList<>();
        if (!playerKing.isFirstMove() || isInCheck()) {
            return kingCastles;
        }

        final int f8 = BoardUtils.INSTANCE.getCoordinateAtPosition("f8");
        final int g8 = BoardUtils.INSTANCE.getCoordinateAtPosition("g8");
        final int h8 = BoardUtils.INSTANCE.getCoordinateAtPosition("h8");
        if (board.getPiece(f8).isEmpty()
                && board.getPiece(g8).isEmpty()) {
            final Optional<Piece> rook = board.getPiece(h8);
            if (rook.isPresent()
                    && rook.get().getPieceAlliance().isBlack()
                    && rook.get().getPieceType() == Piece.PieceType.ROOK
                    && rook.get().isFirstMove()
                    && calculateAttacksOnTile(f8, opponentMoves).isEmpty()
                    && calculateAttacksOnTile(g8, opponentMoves).isEmpty()) {
                kingCastles.add(new Move.KingSideCastleMove(board, playerKing, g8, (Rook) rook.get(), f8));
            }
        }

        final int d8 = BoardUtils.INSTANCE.getCoordinateAtPosition("d8");
        final int c8 = BoardUtils.INSTANCE.getCoordinateAtPosition("c8");
        final int b8 = BoardUtils.INSTANCE.getCoordinateAtPosition("b8");
        final int a8 = BoardUtils.INSTANCE.getCoordinateAtPosition("a8");
        if (board.getPiece(d8).isEmpty()
                && board.getPiece(c8).isEmpty()
                && board.getPiece(b8).isEmpty()) {
            final Optional<Piece> rook = board.getPiece(a8);
            if (rook.isPresent()
                    && rook.get().getPieceAlliance().isBlack()
                    && rook.get().getPieceType() == Piece.PieceType.ROOK
                    && rook.get().isFirstMove()
                    && calculateAttacksOnTile(d8, opponentMoves).isEmpty()
                    && calculateAttacksOnTile(c8, opponentMoves).isEmpty()) {
                kingCastles.add(new Move.QueenSideCastleMove(board, playerKing, c8, (Rook) rook.get(), d8));
            }
        }
        return kingCastles;
    }
}
