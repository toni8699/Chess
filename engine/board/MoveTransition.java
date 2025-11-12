package engine.board;

public class MoveTransition {

    private final Board fromBoard;
    private final Board toBoard;
    private final Move move;

    public MoveTransition(final Board fromBoard,
                          final Board toBoard,
                          final Move move) {
        this.fromBoard = fromBoard;
        this.toBoard = toBoard;
        this.move = move;
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
}
