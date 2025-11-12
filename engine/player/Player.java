package engine.player;

import engine.Alliance;
import engine.board.Board;
import engine.board.Move;

import java.util.Collection;
import java.util.Collections;

public abstract class Player {

    protected final Board board;
    protected final Alliance alliance;
    protected final Collection<Move> legalMoves;

    protected Player(final Board board,
                     final Alliance alliance,
                     final Collection<Move> legalMoves) {
        this.board = board;
        this.alliance = alliance;
        this.legalMoves = Collections.unmodifiableCollection(legalMoves);
    }

    public Alliance getAlliance() {
        return alliance;
    }

    public Collection<Move> getLegalMoves() {
        return legalMoves;
    }

    public Board getBoard() {
        return board;
    }

    public abstract Player getOpponent();
}
