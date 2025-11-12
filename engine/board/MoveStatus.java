package engine.board;

public enum MoveStatus {

    DONE {
        @Override
        public boolean isDone() {
            return true;
        }
    },
    ILLEGAL_MOVE,
    LEAVES_PLAYER_IN_CHECK;

    public boolean isDone() {
        return false;
    }
}
