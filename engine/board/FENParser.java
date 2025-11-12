package engine.board;

import engine.Alliance;
import engine.pieces.Bishop;
import engine.pieces.King;
import engine.pieces.Knight;
import engine.pieces.Pawn;
import engine.pieces.Piece;
import engine.pieces.Queen;
import engine.pieces.Rook;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class FENParser {

    private FENParser() {
    }

    public static Optional<Board> parse(final String fen) {
        if (fen == null || fen.isBlank()) {
            return Optional.empty();
        }
        final String[] tokens = fen.trim().split("\\s+");
        if (tokens.length < 4) {
            return Optional.empty();
        }

        final String piecePlacement = tokens[0];
        final String activeColor = tokens[1];
        final String castlingAvailability = tokens[2];
        final String enPassantTarget = tokens[3];

        final Map<Integer, Piece> boardConfig = new HashMap<>();
        if (!loadPieces(piecePlacement, castlingAvailability, boardConfig)) {
            return Optional.empty();
        }

        final Board.Builder builder = new Board.Builder();
        boardConfig.values().forEach(builder::setPiece);

        final Alliance moveMaker = parseAlliance(activeColor);
        if (moveMaker == null) {
            return Optional.empty();
        }
        builder.setMoveMaker(moveMaker);

        if (!"-".equals(enPassantTarget)) {
            final Integer enPassantCoordinate = coordinateFromAlgebraic(enPassantTarget);
            if (enPassantCoordinate == null) {
                return Optional.empty();
            }
            final int pawnCoordinate = moveMaker.isWhite()
                    ? enPassantCoordinate + BoardUtils.NUM_TILES_PER_ROW
                    : enPassantCoordinate - BoardUtils.NUM_TILES_PER_ROW;
            final Piece pawnPiece = boardConfig.get(pawnCoordinate);
            if (pawnPiece instanceof Pawn) {
                builder.setEnPassantPawn((Pawn) pawnPiece);
            }
        }

        return Optional.of(builder.build());
    }

    private static boolean loadPieces(final String piecePlacement,
                                      final String castlingAvailability,
                                      final Map<Integer, Piece> boardConfig) {
        int coordinate = 0;
        for (int i = 0; i < piecePlacement.length(); i++) {
            final char symbol = piecePlacement.charAt(i);
            if (symbol == '/') {
                continue;
            }
            if (Character.isDigit(symbol)) {
                coordinate += Character.getNumericValue(symbol);
                continue;
            }
            if (coordinate < 0 || coordinate >= BoardUtils.NUM_TILES) {
                return false;
            }
            final Alliance alliance = Character.isUpperCase(symbol) ? Alliance.WHITE : Alliance.BLACK;
            final Piece piece = createPiece(Character.toLowerCase(symbol), alliance, coordinate, castlingAvailability);
            if (piece == null) {
                return false;
            }
            boardConfig.put(coordinate, piece);
            coordinate++;
        }
        return true;
    }

    private static Piece createPiece(final char symbol,
                                     final Alliance alliance,
                                     final int coordinate,
                                     final String castlingAvailability) {
        final boolean firstMove = determineFirstMove(symbol, alliance, coordinate, castlingAvailability);
        return switch (symbol) {
            case 'p' -> new Pawn(alliance, coordinate, firstMove);
            case 'n' -> new Knight(alliance, coordinate, firstMove);
            case 'b' -> new Bishop(alliance, coordinate, firstMove);
            case 'r' -> new Rook(alliance, coordinate, firstMove);
            case 'q' -> new Queen(alliance, coordinate, firstMove);
            case 'k' -> new King(alliance, coordinate, firstMove);
            default -> null;
        };
    }

    private static boolean determineCastlingRight(final String castlingAvailability,
                                                   final char token) {
        return castlingAvailability.indexOf(token) >= 0;
    }

    private static boolean determineFirstMove(final char symbol,
                                              final Alliance alliance,
                                              final int coordinate,
                                              final String castlingAvailability) {
        switch (symbol) {
            case 'p':
                if (alliance.isWhite()) {
                    return coordinate >= BoardUtils.INSTANCE.getCoordinateAtPosition("a2")
                            && coordinate <= BoardUtils.INSTANCE.getCoordinateAtPosition("h2");
                } else {
                    return coordinate >= BoardUtils.INSTANCE.getCoordinateAtPosition("a7")
                            && coordinate <= BoardUtils.INSTANCE.getCoordinateAtPosition("h7");
                }
            case 'k':
                if (alliance.isWhite()) {
                    return determineCastlingRight(castlingAvailability, 'K')
                            || determineCastlingRight(castlingAvailability, 'Q');
                } else {
                    return determineCastlingRight(castlingAvailability, 'k')
                            || determineCastlingRight(castlingAvailability, 'q');
                }
            case 'r':
                if (alliance.isWhite()) {
                    final int kingSide = BoardUtils.INSTANCE.getCoordinateAtPosition("h1");
                    final int queenSide = BoardUtils.INSTANCE.getCoordinateAtPosition("a1");
                    if (coordinate == kingSide) {
                        return determineCastlingRight(castlingAvailability, 'K');
                    }
                    if (coordinate == queenSide) {
                        return determineCastlingRight(castlingAvailability, 'Q');
                    }
                } else {
                    final int kingSide = BoardUtils.INSTANCE.getCoordinateAtPosition("h8");
                    final int queenSide = BoardUtils.INSTANCE.getCoordinateAtPosition("a8");
                    if (coordinate == kingSide) {
                        return determineCastlingRight(castlingAvailability, 'k');
                    }
                    if (coordinate == queenSide) {
                        return determineCastlingRight(castlingAvailability, 'q');
                    }
                }
                return false;
            default:
                return false;
        }
    }

    private static Alliance parseAlliance(final String token) {
        if (token == null) {
            return null;
        }
        final String normalized = token.toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "w" -> Alliance.WHITE;
            case "b" -> Alliance.BLACK;
            default -> null;
        };
    }

    private static Integer coordinateFromAlgebraic(final String notation) {
        try {
            return BoardUtils.INSTANCE.getCoordinateAtPosition(notation);
        } catch (Exception e) {
            return null;
        }
    }
}
