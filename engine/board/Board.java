package engine.board;

import engine.Alliance;
import engine.pieces.Pawn;
import engine.pieces.Piece;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class Board {

    private final Map<Integer, Piece> boardConfig;
    private final Collection<Piece> activePieces;
    private final Alliance currentPlayer;
    private final Pawn enPassantPawn;

    private Board(final Builder builder) {
        this.boardConfig = Collections.unmodifiableMap(new HashMap<>(builder.boardConfig));
        this.activePieces = Collections.unmodifiableCollection(new ArrayList<>(builder.boardConfig.values()));
        this.currentPlayer = builder.nextMoveMaker;
        this.enPassantPawn = builder.enPassantPawn;
    }

    public Optional<Piece> getPiece(final int coordinate) {
        return Optional.ofNullable(boardConfig.get(coordinate));
    }

    public Collection<Piece> getActivePieces() {
        return activePieces;
    }

    public Alliance getCurrentPlayer() {
        return currentPlayer;
    }

    public Optional<Pawn> getEnPassantPawn() {
        return Optional.ofNullable(enPassantPawn);
    }

    public static Board createStandardBoard() {
        return new Builder().build();
    }

    public static final class Builder {
        private final Map<Integer, Piece> boardConfig = new HashMap<>();
        private Alliance nextMoveMaker = Alliance.WHITE;
        private Pawn enPassantPawn;

        public Builder setPiece(final Piece piece) {
            this.boardConfig.put(piece.getPiecePosition(), piece);
            return this;
        }

        public Builder setMoveMaker(final Alliance alliance) {
            this.nextMoveMaker = alliance;
            return this;
        }

        public Builder setEnPassantPawn(final Pawn pawn) {
            this.enPassantPawn = pawn;
            return this;
        }

        public Board build() {
            return new Board(this);
        }
    }
}
