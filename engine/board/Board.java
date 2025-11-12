package engine.board;

import engine.Alliance;
import engine.pieces.Bishop;
import engine.pieces.King;
import engine.pieces.Knight;
import engine.pieces.Pawn;
import engine.pieces.Piece;
import engine.pieces.Queen;
import engine.pieces.Rook;
import engine.player.BlackPlayer;
import engine.player.Player;
import engine.player.WhitePlayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class Board {

    private final List<Tile> gameBoard;
    private final Collection<Piece> whitePieces;
    private final Collection<Piece> blackPieces;
    private final Pawn enPassantPawn;
    private final WhitePlayer whitePlayer;
    private final BlackPlayer blackPlayer;
    private final Player currentPlayer;

    private Board(final Builder builder) {
        this.gameBoard = createGameBoard(builder);
        this.whitePieces = calculateActivePieces(this.gameBoard, Alliance.WHITE);
        this.blackPieces = calculateActivePieces(this.gameBoard, Alliance.BLACK);
        this.enPassantPawn = builder.enPassantPawn;

        final Collection<Move> whiteStandardLegalMoves = calculateLegalMoves(this.whitePieces);
        final Collection<Move> blackStandardLegalMoves = calculateLegalMoves(this.blackPieces);

        this.whitePlayer = new WhitePlayer(this, whiteStandardLegalMoves, blackStandardLegalMoves);
        this.blackPlayer = new BlackPlayer(this, blackStandardLegalMoves, whiteStandardLegalMoves);
        this.currentPlayer = builder.nextMoveMaker == Alliance.WHITE ? this.whitePlayer : this.blackPlayer;
    }

    private static List<Tile> createGameBoard(final Builder builder) {
        final Tile[] tiles = new Tile[BoardUtils.NUM_TILES];
        for (int i = 0; i < BoardUtils.NUM_TILES; i++) {
            final Piece piece = builder.boardConfig.get(i);
            tiles[i] = piece != null ? new Tile.OccupiedTile(i, piece) : new Tile.EmptyTile(i);
        }
        return Collections.unmodifiableList(List.of(tiles));
    }

    private static Collection<Piece> calculateActivePieces(final List<Tile> gameBoard,
                                                           final Alliance alliance) {
        final List<Piece> activePieces = new ArrayList<>();
        for (final Tile tile : gameBoard) {
            if (tile.isTileOccupied()) {
                final Piece piece = tile.getPiece();
                if (piece.getPieceAlliance() == alliance) {
                    activePieces.add(piece);
                }
            }
        }
        return Collections.unmodifiableList(activePieces);
    }

    private Collection<Move> calculateLegalMoves(final Collection<Piece> pieces) {
        final List<Move> legalMoves = new ArrayList<>();
        for (final Piece piece : pieces) {
            legalMoves.addAll(piece.calculateLegalMoves(this));
        }
        return Collections.unmodifiableList(legalMoves);
    }

    public Optional<Piece> getPiece(final int coordinate) {
        return Optional.ofNullable(gameBoard.get(coordinate).getPiece());
    }

    public Collection<Piece> getWhitePieces() {
        return whitePieces;
    }

    public Collection<Piece> getBlackPieces() {
        return blackPieces;
    }

    public Collection<Piece> getAllPieces() {
        final List<Piece> allPieces = new ArrayList<>(whitePieces.size() + blackPieces.size());
        allPieces.addAll(whitePieces);
        allPieces.addAll(blackPieces);
        return Collections.unmodifiableList(allPieces);
    }

    public Pawn getEnPassantPawnValue() {
        return enPassantPawn;
    }

    public Optional<Pawn> getEnPassantPawn() {
        return Optional.ofNullable(enPassantPawn);
    }

    public WhitePlayer getWhitePlayer() {
        return whitePlayer;
    }

    public BlackPlayer getBlackPlayer() {
        return blackPlayer;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public static Board createStandardBoard() {
        final Builder builder = new Builder();

        // Black pieces
        builder.setPiece(new Rook(Alliance.BLACK, 0, true));
        builder.setPiece(new Knight(Alliance.BLACK, 1, true));
        builder.setPiece(new Bishop(Alliance.BLACK, 2, true));
        builder.setPiece(new Queen(Alliance.BLACK, 3, true));
        builder.setPiece(new King(Alliance.BLACK, 4, true));
        builder.setPiece(new Bishop(Alliance.BLACK, 5, true));
        builder.setPiece(new Knight(Alliance.BLACK, 6, true));
        builder.setPiece(new Rook(Alliance.BLACK, 7, true));
        builder.setPiece(new Pawn(Alliance.BLACK, 8, true));
        builder.setPiece(new Pawn(Alliance.BLACK, 9, true));
        builder.setPiece(new Pawn(Alliance.BLACK, 10, true));
        builder.setPiece(new Pawn(Alliance.BLACK, 11, true));
        builder.setPiece(new Pawn(Alliance.BLACK, 12, true));
        builder.setPiece(new Pawn(Alliance.BLACK, 13, true));
        builder.setPiece(new Pawn(Alliance.BLACK, 14, true));
        builder.setPiece(new Pawn(Alliance.BLACK, 15, true));

        // White pieces
        builder.setPiece(new Pawn(Alliance.WHITE, 48, true));
        builder.setPiece(new Pawn(Alliance.WHITE, 49, true));
        builder.setPiece(new Pawn(Alliance.WHITE, 50, true));
        builder.setPiece(new Pawn(Alliance.WHITE, 51, true));
        builder.setPiece(new Pawn(Alliance.WHITE, 52, true));
        builder.setPiece(new Pawn(Alliance.WHITE, 53, true));
        builder.setPiece(new Pawn(Alliance.WHITE, 54, true));
        builder.setPiece(new Pawn(Alliance.WHITE, 55, true));
        builder.setPiece(new Rook(Alliance.WHITE, 56, true));
        builder.setPiece(new Knight(Alliance.WHITE, 57, true));
        builder.setPiece(new Bishop(Alliance.WHITE, 58, true));
        builder.setPiece(new Queen(Alliance.WHITE, 59, true));
        builder.setPiece(new King(Alliance.WHITE, 60, true));
        builder.setPiece(new Bishop(Alliance.WHITE, 61, true));
        builder.setPiece(new Knight(Alliance.WHITE, 62, true));
        builder.setPiece(new Rook(Alliance.WHITE, 63, true));

        builder.setMoveMaker(Alliance.WHITE);
        return builder.build();
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
