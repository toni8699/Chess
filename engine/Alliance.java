package engine;

import engine.board.BoardUtils;

public enum Alliance {

    WHITE() {

        @Override
        public boolean isWhite() {
            return true;
        }

        @Override
        public boolean isBlack() {
            return false;
        }

        @Override
        public int getDirection() {
            return UP_DIRECTION;
        }

        @Override
        public int getOppositeDirection() {
            return DOWN_DIRECTION;
        }

        @Override
        public boolean isPawnPromotionSquare(final int position) {
            return BoardUtils.FIRST_ROW.get(position);
        }

        @Override
        public Alliance choosePlayerByAlliance(final Alliance whitePlayer,
                                               final Alliance blackPlayer) {
            return whitePlayer;
        }

        @Override
        public String toString() {
            return "White";
        }
    },
    BLACK() {

        @Override
        public boolean isWhite() {
            return false;
        }

        @Override
        public boolean isBlack() {
            return true;
        }

        @Override
        public int getDirection() {
            return DOWN_DIRECTION;
        }

        @Override
        public int getOppositeDirection() {
            return UP_DIRECTION;
        }

        @Override
        public boolean isPawnPromotionSquare(final int position) {
            return BoardUtils.EIGHTH_ROW.get(position);
        }

        @Override
        public Alliance choosePlayerByAlliance(final Alliance whitePlayer,
                                               final Alliance blackPlayer) {
            return blackPlayer;
        }

        @Override
        public String toString() {
            return "Black";
        }
    };

    private static final int UP_DIRECTION = -1;
    private static final int DOWN_DIRECTION = 1;

    public abstract boolean isWhite();
    public abstract boolean isBlack();
    public abstract int getDirection();
    public abstract int getOppositeDirection();
    public abstract boolean isPawnPromotionSquare(int position);
    public abstract Alliance choosePlayerByAlliance(Alliance whitePlayer, Alliance blackPlayer);
}
