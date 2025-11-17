package engine.pieces;

import engine.Alliance;
import engine.board.Board;
import engine.board.BoardUtils;
import engine.board.Move;
import engine.board.Move.PawnAttackMove;
import engine.board.Move.PawnEnPassantAttackMove;
import engine.board.Move.PawnJump;
import engine.board.Move.PawnMove;
import engine.board.Move.PawnPromotion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Pawn extends Piece {

    private static final int[] CANDIDATE_MOVE_COORDINATES = {8, 16, 7, 9};

    public Pawn(final Alliance alliance,
                final int piecePosition,
                final boolean isFirstMove) {
        super(PieceType.PAWN, alliance, piecePosition, isFirstMove);
    }

    @Override
    public Collection<Move> calculateLegalMoves(final Board board) {
        final List<Move> legalMoves = new ArrayList<>();
        for (final int currentCandidateOffset : CANDIDATE_MOVE_COORDINATES) {
            final int candidateDestination = this.piecePosition + (this.pieceAlliance.getDirection() * currentCandidateOffset);
            if (!BoardUtils.isValidTileCoordinate(candidateDestination)) {
                continue;
            }
            if (currentCandidateOffset == 8 && board.getPiece(candidateDestination).isEmpty()) {
                addPawnMove(board, legalMoves, candidateDestination);
            } else if (currentCandidateOffset == 16 && isFirstMove()) {
                final int behindDestination = this.piecePosition + (this.pieceAlliance.getDirection() * 8);
                if ((BoardUtils.SECOND_ROW.get(this.piecePosition) && this.pieceAlliance.isBlack()) ||
                        (BoardUtils.SEVENTH_ROW.get(this.piecePosition) && this.pieceAlliance.isWhite())) {
                    if (board.getPiece(behindDestination).isEmpty() && board.getPiece(candidateDestination).isEmpty()) {
                        legalMoves.add(new PawnJump(board, this, candidateDestination));
                    }
                }
            } else if (currentCandidateOffset == 7 &&
                    !((BoardUtils.EIGHTH_COLUMN.get(this.piecePosition) && this.pieceAlliance.isWhite()) ||
                            (BoardUtils.FIRST_COLUMN.get(this.piecePosition) && this.pieceAlliance.isBlack()))) {
                addPawnAttackMoves(board, legalMoves, candidateDestination);
            } else if (currentCandidateOffset == 9 &&
                    !((BoardUtils.FIRST_COLUMN.get(this.piecePosition) && this.pieceAlliance.isWhite()) ||
                            (BoardUtils.EIGHTH_COLUMN.get(this.piecePosition) && this.pieceAlliance.isBlack()))) {
                addPawnAttackMoves(board, legalMoves, candidateDestination);
            }
        }
        return legalMoves;
    }

    private void addPawnMove(final Board board,
                             final List<Move> legalMoves,
                             final int destination) {
        if (this.pieceAlliance.isPawnPromotionSquare(destination)) {
            addPromotionMoves(board, legalMoves, new PawnMove(board, this, destination));
        } else {
            legalMoves.add(new PawnMove(board, this, destination));
        }
    }

    private void addPawnAttackMoves(final Board board,
                                    final List<Move> legalMoves,
                                    final int destination) {
        final var pieceOnCandidate = board.getPiece(destination);
        pieceOnCandidate.ifPresent(opponentPiece -> {
            if (opponentPiece.getPieceAlliance() != this.pieceAlliance) {
                if (this.pieceAlliance.isPawnPromotionSquare(destination)) {
                    addPromotionMoves(board, legalMoves,
                            new PawnAttackMove(board, this, destination, opponentPiece));
                } else {
                    legalMoves.add(new PawnAttackMove(board, this, destination, opponentPiece));
                }
            }
        });
        board.getEnPassantPawn().ifPresent(enPassantPawn -> {
            if (enPassantPawn.getPieceAlliance() != this.pieceAlliance &&
                    enPassantPawn.getPiecePosition() == this.piecePosition + this.pieceAlliance.getOppositeDirection()) {
                legalMoves.add(new PawnEnPassantAttackMove(board, this, destination, enPassantPawn));
            }
        });
    }

    public boolean hasMovedTwoSquares(final Move move) {
        return Math.abs(move.getDestinationCoordinate() - this.piecePosition) == 16;
    }

    @Override
    public Pawn movePiece(final Move move) {
        return new Pawn(move.getMovedPiece().getPieceAlliance(), move.getDestinationCoordinate(), false);
    }

    private void addPromotionMoves(final Board board,
                                   final List<Move> legalMoves,
                                   final Move decoratedMove) {
        final int destination = decoratedMove.getDestinationCoordinate();
        legalMoves.add(new PawnPromotion(decoratedMove, new Queen(this.pieceAlliance, destination, false)));
        legalMoves.add(new PawnPromotion(decoratedMove, new Rook(this.pieceAlliance, destination, false)));
        legalMoves.add(new PawnPromotion(decoratedMove, new Bishop(this.pieceAlliance, destination, false)));
        legalMoves.add(new PawnPromotion(decoratedMove, new Knight(this.pieceAlliance, destination, false)));
    }
}
