package engine.board;

import engine.Alliance;
import engine.pieces.Piece;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class Board {

    private final Map<Integer, Piece> boardConfig;
    private final Alliance currentPlayer;

    private Board(final Builder builder) {
        this.boardConfig = Collections.unmodifiableMap(new HashMap<>(builder.boardConfig));
        this.currentPlayer = builder.nextMoveMaker;
    }

    public Optional<Piece> getPiece(final int coordinate) {
        return Optional.ofNullable(boardConfig.get(coordinate));
    }

    public Alliance getCurrentPlayer() {
        return currentPlayer;
    }

    public static Board createStandardBoard() {
        return new Builder().build();
    }

    public static final class Builder {
        private final Map<Integer, Piece> boardConfig = new HashMap<>();
        private Alliance nextMoveMaker = Alliance.WHITE;

        public Builder setPiece(final Piece piece) {
            this.boardConfig.put(piece.getPiecePosition(), piece);
            return this;
        }

        public Builder setMoveMaker(final Alliance alliance) {
            this.nextMoveMaker = alliance;
            return this;
        }

        public Board build() {
            return new Board(this);
        }
    }
}
