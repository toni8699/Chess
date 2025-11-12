package ui;

import engine.Alliance;
import engine.board.BoardUtils;
import engine.board.Move;
import engine.pieces.Piece;
import engine.player.Player;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.input.MouseEvent;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class GamePanel extends BorderPane {

    private static final int TILE_SIZE = 100;
    private static final int BOARD_PIXELS = TILE_SIZE * BoardUtils.NUM_TILES_PER_ROW;
    private static final String STARTING_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq -";

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

    private Piece draggingPiece;
    private int draggingStartCoordinate = -1;
    private double dragOffsetX;
    private double dragOffsetY;
    private double dragX;
    private double dragY;
    private boolean dragActive;
    private boolean pressSelected;
    private int pressCoordinate = -1;
    private boolean pressWasSelected;

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

        boardCanvas.setOnMousePressed(this::handleMousePressed);
        boardCanvas.setOnMouseDragged(this::handleMouseDragged);
        boardCanvas.setOnMouseReleased(this::handleMouseReleased);

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

        final Button loadFenButton = new Button("Load FEN");
        loadFenButton.setMaxWidth(Double.MAX_VALUE);
        loadFenButton.setOnAction(e -> showFenDialog());
        final Button loadPgnButton = new Button("Load PGN");
        loadPgnButton.setMaxWidth(Double.MAX_VALUE);
        loadPgnButton.setOnAction(e -> showPgnDialog());
        box.getChildren().addAll(loadFenButton, loadPgnButton);

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

    private void showFenDialog() {
        final TextInputDialog dialog = new TextInputDialog(STARTING_FEN);
        dialog.setTitle("Load FEN");
        dialog.setHeaderText("Paste a FEN position");
        dialog.setContentText("FEN:");
        dialog.showAndWait().ifPresent(fen -> {
            if (fen == null || fen.isBlank()) {
                return;
            }
            final boolean loaded = model.loadPositionFromFen(fen.trim());
            if (!loaded) {
                final Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Invalid FEN");
                alert.setHeaderText(null);
                alert.setContentText("The provided FEN string could not be parsed.");
                alert.showAndWait();
                return;
            }
            setBoardFlipped(model.getBoard().getCurrentPlayer().getAlliance().isBlack());
            clearSelection();
            refreshMoveHistory();
            updateStatusLabel();
            redraw();
        });
    }

    private void showPgnDialog() {
        final Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Load PGN");
        dialog.setHeaderText("Paste a PGN game");
        final ButtonType loadButton = new ButtonType("Load", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loadButton, ButtonType.CANCEL);
        final TextArea textArea = new TextArea();
        textArea.setPromptText("[Event \"...\"]\n1. e4 e5 2. Nf3 ...");
        textArea.setWrapText(true);
        textArea.setPrefRowCount(12);
        dialog.getDialogPane().setContent(textArea);
        dialog.getDialogPane().setPrefWidth(480);
        dialog.setResultConverter(button -> button == loadButton ? textArea.getText() : null);
        dialog.showAndWait().ifPresent(pgn -> {
            if (pgn == null || pgn.isBlank()) {
                return;
            }
            final boolean loaded = model.loadGameFromPgn(pgn);
            if (!loaded) {
                final Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Invalid PGN");
                alert.setHeaderText(null);
                alert.setContentText("The PGN text could not be parsed or contained illegal moves.");
                alert.showAndWait();
                return;
            }
            setBoardFlipped(model.getBoard().getCurrentPlayer().getAlliance().isBlack());
            clearSelection();
            refreshMoveHistory();
            updateStatusLabel();
            redraw();
        });
    }

    private void setBoardFlipped(final boolean flipped) {
        if (this.boardFlipped != flipped) {
            this.boardFlipped = flipped;
        }
    }

    private void handleMousePressed(final MouseEvent event) {
        final int coordinate = screenToCoordinate(event.getX(), event.getY());
        pressSelected = false;
        pressCoordinate = coordinate;
        pressWasSelected = coordinate != -1 && coordinate == selectedSquare;
        dragActive = false;
        draggingPiece = null;
        draggingStartCoordinate = -1;

        if (coordinate == -1) {
            return;
        }

        final Optional<Piece> piece = model.getBoard().getPiece(coordinate);
        if (piece.isPresent() && piece.get().getPieceAlliance() == model.getBoard().getCurrentPlayer().getAlliance()) {
            selectSquare(coordinate);
            pressSelected = true;
            draggingPiece = piece.get();
            draggingStartCoordinate = coordinate;
            final double[] screen = coordinateToScreen(coordinate);
            dragOffsetX = event.getX() - screen[0];
            dragOffsetY = event.getY() - screen[1];
        }
    }

    private void handleMouseDragged(final MouseEvent event) {
        if (draggingPiece == null || draggingStartCoordinate == -1) {
            return;
        }
        dragActive = true;
        dragX = event.getX() - dragOffsetX;
        dragY = event.getY() - dragOffsetY;
        redraw();
    }

    private void handleMouseReleased(final MouseEvent event) {
        if (!dragActive || draggingPiece == null || draggingStartCoordinate == -1) {
            final int destination = screenToCoordinate(event.getX(), event.getY());
            final boolean skipClick = pressSelected && !pressWasSelected
                    && destination == pressCoordinate
                    && destination == selectedSquare;
            dragActive = false;
            draggingPiece = null;
            draggingStartCoordinate = -1;
            pressSelected = false;
            pressCoordinate = -1;
            pressWasSelected = false;
            if (!skipClick) {
                handleBoardClick(event.getX(), event.getY());
            }
            return;
        }

        final int origin = draggingStartCoordinate;
        final int destination = screenToCoordinate(event.getX(), event.getY());

        dragActive = false;
        draggingPiece = null;
        draggingStartCoordinate = -1;
        pressSelected = false;
        pressCoordinate = -1;
        pressWasSelected = false;

        if (destination == -1) {
            selectSquare(origin);
            return;
        }

        if (!attemptMove(destination)) {
            selectSquare(origin);
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

        if (!attemptMove(coordinate)) {
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
        clearSelection();
        refreshMoveHistory();
        updateStatusLabel();
        redraw();
        return true;
    }

    private void clearSelection() {
        selectedSquare = -1;
        highlightedMoves = List.of();
        dragActive = false;
        draggingPiece = null;
        draggingStartCoordinate = -1;
        pressSelected = false;
        pressCoordinate = -1;
        pressWasSelected = false;
    }

    private void redraw() {
        drawBoardTiles();
        drawHighlights();
        drawPieces();
        drawCheckIndicator();
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
            final boolean isDraggedPiece = dragActive && draggingPiece != null && coordinate == draggingStartCoordinate;
            if (!isDraggedPiece) {
                gc.drawImage(image, coords[0], coords[1], TILE_SIZE, TILE_SIZE);
            }
        }

        if (dragActive && draggingPiece != null) {
            final Image image = getImageForPiece(draggingPiece);
            gc.drawImage(image, dragX, dragY, TILE_SIZE, TILE_SIZE);
        }
    }

    private void drawCheckIndicator() {
        final Player currentPlayer = model.getBoard().getCurrentPlayer();
        if (currentPlayer.isInCheck()) {
            final int kingCoordinate = currentPlayer.getPlayerKing().getPiecePosition();
            final double[] coords = coordinateToScreen(kingCoordinate);
            gc.setFill(Color.color(1, 0, 0, 0.35));
            gc.fillRect(coords[0], coords[1], TILE_SIZE, TILE_SIZE);
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
