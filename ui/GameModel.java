package ui;

import engine.board.Board;
import engine.board.BoardUtils;
import engine.board.Move;
import engine.board.MoveTransition;
import engine.player.Player;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class GameModel {

    private Board currentBoard;
    private final Deque<Board> history;
    private final List<String> moveHistory;

    public GameModel() {
        this.currentBoard = Board.createStandardBoard();
        this.history = new ArrayDeque<>();
        this.moveHistory = new ArrayList<>();
    }

    public Board getBoard() {
        return currentBoard;
    }

    public Collection<Move> getLegalMovesFrom(final int sourceCoordinate) {
        return currentBoard.getCurrentPlayer().getLegalMoves().stream()
                .filter(move -> move.getMovedPiece().getPiecePosition() == sourceCoordinate)
                .collect(Collectors.toUnmodifiableList());
    }

    public boolean makeMove(final int sourceCoordinate, final int destinationCoordinate) {
        final Optional<Move> candidateMove = currentBoard.getCurrentPlayer().getLegalMoves().stream()
                .filter(move -> move.getMovedPiece().getPiecePosition() == sourceCoordinate
                        && move.getDestinationCoordinate() == destinationCoordinate)
                .findFirst();
        if (candidateMove.isEmpty()) {
            return false;
        }

        final MoveTransition transition = currentBoard.getCurrentPlayer().makeMove(candidateMove.get());
        if (!transition.getMoveStatus().isDone()) {
            return false;
        }

        history.push(currentBoard);
        currentBoard = transition.getToBoard();
        moveHistory.add(describeMove(candidateMove.get(), transition.getToBoard().getCurrentPlayer().getOpponent()));
        return true;
    }

    public boolean undo() {
        if (history.isEmpty()) {
            return false;
        }
        currentBoard = history.pop();
        if (!moveHistory.isEmpty()) {
            moveHistory.remove(moveHistory.size() - 1);
        }
        return true;
    }

    public void reset() {
        currentBoard = Board.createStandardBoard();
        history.clear();
        moveHistory.clear();
    }

    public List<String> getMoveHistory() {
        return List.copyOf(moveHistory);
    }

    private String describeMove(final Move move, final Player movingPlayer) {
        final String pieceSymbol = move.getMovedPiece().getPieceType().toString();
        final String from = BoardUtils.INSTANCE.getPositionAtCoordinate(move.getMovedPiece().getPiecePosition());
        final String to = BoardUtils.INSTANCE.getPositionAtCoordinate(move.getDestinationCoordinate());
        final boolean capture = move.isAttack();
        final boolean promotion = move instanceof Move.PawnPromotion;
        final StringBuilder builder = new StringBuilder();
        builder.append(movingPlayer.getAlliance().toString().charAt(0)).append(": ");
        if (!pieceSymbol.equals("P")) {
            builder.append(pieceSymbol);
        }
        builder.append(from);
        builder.append(capture ? "x" : "-");
        builder.append(to);
        if (promotion) {
            final Move.PawnPromotion promotionMove = (Move.PawnPromotion) move;
            builder.append("=").append(promotionMove.getPromotionPiece().getPieceType().toString());
        }
        return builder.toString();
    }
}
