package ui;

import engine.Alliance;
import engine.board.BoardUtils;
import engine.board.Move;
import engine.pieces.Piece;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class GamePanel extends BorderPane {

    private static final int TILE_SIZE = 100;
    private static final int BOARD_PIXELS = TILE_SIZE * BoardUtils.NUM_TILES_PER_ROW;

    private final GameModel model;
    private final Canvas boardCanvas;
    private final GraphicsContext gc;
    private final VBox rightColumn;
    private final ObservableList<String> moveHistoryData;
    private final ListView<String> moveHistoryView;
    private final Label statusLabel;

    private final Map<Alliance, Map<Piece.PieceType, Image>> imageCache = new EnumMap<>(Alliance.class);

    private boolean boardFlipped = false;
    private int selectedSquare = -1;
    private List<Move> highlightedMoves = List.of();
    private BoardColorScheme boardColorScheme = BoardColorScheme.CLASSIC;

    public GamePanel(final GameModel model) {
        this.model = model;
        this.boardCanvas = new Canvas(BOARD_PIXELS, BOARD_PIXELS);
        this.gc = boardCanvas.getGraphicsContext2D();
        this.moveHistoryData = FXCollections.observableArrayList();
        this.moveHistoryView = new ListView<>(moveHistoryData);
        this.statusLabel = new Label();
        this.rightColumn = buildRightColumn();

        initializePieceImages();
        setCenter(boardCanvas);
        setRight(rightColumn);

        boardCanvas.setOnMouseClicked(event -> handleBoardClick(event.getX(), event.getY()));

        refreshMoveHistory();
        updateStatusLabel();
        redraw();
    }

    private VBox buildRightColumn() {
        final VBox box = new VBox(10);
        box.setAlignment(Pos.TOP_CENTER);
        box.setStyle("-fx-padding: 15;");

        box.getChildren().add(statusLabel);

        final Separator separator = new Separator();
        separator.setStyle("-fx-padding: 10 0 10 0;");
        box.getChildren().add(separator);

        final Label colorLabel = new Label("Board Colors");
        colorLabel.setStyle("-fx-font-weight: bold;");
        final ComboBox<BoardColorScheme> colorSelector = new ComboBox<>();
        colorSelector.getItems().setAll(BoardColorScheme.values());
        colorSelector.setValue(boardColorScheme);
        colorSelector.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                boardColorScheme = newValue;
                redraw();
            }
        });
        box.getChildren().addAll(colorLabel, colorSelector);

        final Separator separatorHistory = new Separator();
        separatorHistory.setStyle("-fx-padding: 10 0 10 0;");
        box.getChildren().add(separatorHistory);

        final Label historyLabel = new Label("Move History");
        historyLabel.setStyle("-fx-font-weight: bold;");
        box.getChildren().add(historyLabel);

        moveHistoryView.setPrefHeight(300);
        box.getChildren().add(moveHistoryView);
        VBox.setVgrow(moveHistoryView, Priority.NEVER);

        final Separator separator2 = new Separator();
        separator2.setStyle("-fx-padding: 10 0 10 0;");
        box.getChildren().add(separator2);

        final Button setupButton = new Button("Setup");
        setupButton.setMaxWidth(Double.MAX_VALUE);
        setupButton.setOnAction(e -> showSetupDialog());

        final Button undoButton = new Button("Undo");
        undoButton.setMaxWidth(Double.MAX_VALUE);
        undoButton.setOnAction(e -> {
            if (model.undo()) {
                clearSelection();
                refreshMoveHistory();
                updateStatusLabel();
                redraw();
            }
        });

        final Button restartButton = new Button("Restart");
        restartButton.setMaxWidth(Double.MAX_VALUE);
        restartButton.setOnAction(e -> {
            model.reset();
            clearSelection();
            refreshMoveHistory();
            updateStatusLabel();
            redraw();
        });

        box.getChildren().addAll(setupButton, undoButton, restartButton);
        return box;
    }

    private void showSetupDialog() {
        final ChoiceDialog<String> dialog = new ChoiceDialog<>("White", List.of("White", "Black"));
        dialog.setTitle("Game Setup");
        dialog.setHeaderText(null);
        dialog.setContentText("Play as:");
        dialog.showAndWait().ifPresent(choice -> {
            setBoardFlipped("Black".equalsIgnoreCase(choice));
            clearSelection();
            redraw();
        });
    }

    private void setBoardFlipped(final boolean flipped) {
        if (this.boardFlipped != flipped) {
            this.boardFlipped = flipped;
        }
    }

    private void handleBoardClick(final double x, final double y) {
        final int coordinate = screenToCoordinate(x, y);
        if (coordinate == -1) {
            clearSelection();
            redraw();
            return;
        }

        if (selectedSquare == -1) {
            selectSquare(coordinate);
            return;
        }

        if (coordinate == selectedSquare) {
            clearSelection();
            redraw();
            return;
        }

        if (attemptMove(coordinate)) {
            clearSelection();
            refreshMoveHistory();
            updateStatusLabel();
            redraw();
        } else {
            selectSquare(coordinate);
        }
    }

    private void selectSquare(final int coordinate) {
        final Optional<Piece> piece = model.getBoard().getPiece(coordinate);
        if (piece.isPresent() && piece.get().getPieceAlliance() == model.getBoard().getCurrentPlayer().getAlliance()) {
            selectedSquare = coordinate;
            highlightedMoves = new ArrayList<>(model.getLegalMovesFrom(coordinate));
        } else {
            clearSelection();
        }
        redraw();
    }

    private boolean attemptMove(final int destinationCoordinate) {
        if (selectedSquare == -1) {
            return false;
        }
        final boolean moved = model.makeMove(selectedSquare, destinationCoordinate);
        if (!moved) {
            return false;
        }
        return true;
    }

    private void clearSelection() {
        selectedSquare = -1;
        highlightedMoves = List.of();
    }

    private void redraw() {
        drawBoardTiles();
        drawHighlights();
        drawPieces();
    }

    private void drawBoardTiles() {
        gc.clearRect(0, 0, BOARD_PIXELS, BOARD_PIXELS);
        for (int row = 0; row < BoardUtils.NUM_TILES_PER_ROW; row++) {
            for (int col = 0; col < BoardUtils.NUM_TILES_PER_ROW; col++) {
                final boolean lightSquare = (row + col) % 2 == 0;
                gc.setFill(lightSquare ? boardColorScheme.getLightColor() : boardColorScheme.getDarkColor());
                final double x = tileToScreenCol(col);
                final double y = tileToScreenRow(row);
                gc.fillRect(x, y, TILE_SIZE, TILE_SIZE);
            }
        }
    }

    private void drawHighlights() {
        if (selectedSquare >= 0) {
            final double[] selected = coordinateToScreen(selectedSquare);
            gc.setStroke(Color.GOLD);
            gc.setLineWidth(3.0);
            gc.strokeRect(selected[0] + 2, selected[1] + 2, TILE_SIZE - 4, TILE_SIZE - 4);
        }

        gc.setFill(Color.color(0, 1, 0, 0.35));
        for (final Move move : highlightedMoves) {
            final double[] coords = coordinateToScreen(move.getDestinationCoordinate());
            gc.fillRect(coords[0], coords[1], TILE_SIZE, TILE_SIZE);
        }
    }

    private void drawPieces() {
        for (int coordinate = 0; coordinate < BoardUtils.NUM_TILES; coordinate++) {
            final Optional<Piece> piece = model.getBoard().getPiece(coordinate);
            if (piece.isEmpty()) {
                continue;
            }
            final Image image = getImageForPiece(piece.get());
            final double[] coords = coordinateToScreen(coordinate);
            gc.drawImage(image, coords[0], coords[1], TILE_SIZE, TILE_SIZE);
        }
    }

    private void refreshMoveHistory() {
        moveHistoryData.setAll(model.getMoveHistory());
    }

    private void updateStatusLabel() {
        final var currentPlayer = model.getBoard().getCurrentPlayer();
        final StringBuilder status = new StringBuilder(currentPlayer.getAlliance().toString()).append(" to move");
        if (currentPlayer.isInCheckMate()) {
            status.setLength(0);
            status.append(currentPlayer.getOpponent().getAlliance()).append(" wins by checkmate");
        } else if (currentPlayer.isInStaleMate()) {
            status.append(" (Stalemate)");
        } else if (currentPlayer.isInCheck()) {
            status.append(" (Check)");
        }
        statusLabel.setText(status.toString());
    }

    private double tileToScreenCol(final int boardCol) {
        final int col = boardFlipped ? BoardUtils.NUM_TILES_PER_ROW - 1 - boardCol : boardCol;
        return col * TILE_SIZE;
    }

    private double tileToScreenRow(final int boardRow) {
        final int row = boardFlipped ? BoardUtils.NUM_TILES_PER_ROW - 1 - boardRow : boardRow;
        return row * TILE_SIZE;
    }

    private double[] coordinateToScreen(final int coordinate) {
        final int row = coordinate / BoardUtils.NUM_TILES_PER_ROW;
        final int col = coordinate % BoardUtils.NUM_TILES_PER_ROW;
        return new double[]{tileToScreenCol(col), tileToScreenRow(row)};
    }

    private int screenToCoordinate(final double x, final double y) {
        if (x < 0 || y < 0 || x >= BOARD_PIXELS || y >= BOARD_PIXELS) {
            return -1;
        }
        int col = (int) (x / TILE_SIZE);
        int row = (int) (y / TILE_SIZE);
        if (boardFlipped) {
            col = BoardUtils.NUM_TILES_PER_ROW - 1 - col;
            row = BoardUtils.NUM_TILES_PER_ROW - 1 - row;
        }
        final int coordinate = row * BoardUtils.NUM_TILES_PER_ROW + col;
        return BoardUtils.isValidTileCoordinate(coordinate) ? coordinate : -1;
    }

    private Image getImageForPiece(final Piece piece) {
        final Map<Piece.PieceType, Image> cacheForAlliance = imageCache.computeIfAbsent(
                piece.getPieceAlliance(),
                alliance -> new EnumMap<>(Piece.PieceType.class));
        return cacheForAlliance.computeIfAbsent(piece.getPieceType(), type -> loadImage(piece.getPieceAlliance(), type));
    }

    private Image loadImage(final Alliance alliance, final Piece.PieceType pieceType) {
        final String color = alliance.isWhite() ? "white" : "black";
        final String name = switch (pieceType) {
            case KING -> "king";
            case QUEEN -> "queen";
            case ROOK -> "rook";
            case BISHOP -> "bishop";
            case KNIGHT -> "knight";
            case PAWN -> "pawn";
        };
        final Path path = Paths.get(System.getProperty("user.dir"), "res", "pieces-basic-png", color + "-" + name + ".png");
        try (InputStream inputStream = Files.newInputStream(path)) {
            return new Image(inputStream);
        } catch (IOException e) {
            final Canvas placeholderCanvas = new Canvas(TILE_SIZE, TILE_SIZE);
            final GraphicsContext context = placeholderCanvas.getGraphicsContext2D();
            context.setFill(alliance.isWhite() ? Color.WHITESMOKE : Color.DARKSLATEGRAY);
            context.fillRect(0, 0, TILE_SIZE, TILE_SIZE);
            return placeholderCanvas.snapshot(null, null);
        }
    }

    private void initializePieceImages() {
        for (Alliance alliance : Alliance.values()) {
            final Map<Piece.PieceType, Image> cache = imageCache.computeIfAbsent(alliance, key -> new EnumMap<>(Piece.PieceType.class));
            for (Piece.PieceType type : Piece.PieceType.values()) {
                cache.putIfAbsent(type, loadImage(alliance, type));
            }
        }
    }

    private enum BoardColorScheme {
        CLASSIC("Classic", Color.BEIGE, Color.SADDLEBROWN),
        LEGACY_BLUE("Blue & White", Color.ALICEBLUE, Color.DARKSLATEBLUE),
        GREEN("Green", Color.WHITESMOKE, Color.DARKOLIVEGREEN);

        private final String displayName;
        private final Color lightColor;
        private final Color darkColor;

        BoardColorScheme(final String displayName, final Color lightColor, final Color darkColor) {
            this.displayName = displayName;
            this.lightColor = lightColor;
            this.darkColor = darkColor;
        }

        public Color getLightColor() {
            return lightColor;
        }

        public Color getDarkColor() {
            return darkColor;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }
}
