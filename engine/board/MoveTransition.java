package engine.board;

public class MoveTransition {

    private final Board fromBoard;
    private final Board toBoard;
    private final Move move;
    private final MoveStatus moveStatus;

    public MoveTransition(final Board fromBoard,
                          final Board toBoard,
                          final Move move,
                          final MoveStatus moveStatus) {
        this.fromBoard = fromBoard;
        this.toBoard = toBoard;
        this.move = move;
        this.moveStatus = moveStatus;
    }

    public Board getFromBoard() {
        return fromBoard;
    }

    public Board getToBoard() {
        return toBoard;
    }

    public Move getMove() {
        return move;
    }

    public MoveStatus getMoveStatus() {
        return moveStatus;
    }
}
