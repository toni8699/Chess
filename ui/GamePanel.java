package ui;

import engine.Alliance;
import engine.board.BoardUtils;
import engine.board.Move;
import engine.pieces.Piece;
import engine.player.Player;
import engine.pieces.Piece.PieceType;
import javafx.animation.AnimationTimer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
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
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

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

/**
 * JavaFX front-end that renders the immutable engine state and mediates
 * all human/AI interaction, drag logic, dialogs, and side panels.
 */
public class GamePanel extends BorderPane {

    // Rendering constants for board layout
    private static final int TILE_SIZE = 100;
    private static final int BOARD_PIXELS = TILE_SIZE * BoardUtils.NUM_TILES_PER_ROW;
    private static final long MOVE_ANIMATION_DURATION_NANOS = 180_000_000L; // ~180 ms travel time
    private static final String STARTING_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq -";

    // Adapter between immutable engine and JavaFX view
    private final GameModel model;
    private final Canvas boardCanvas;
    private final GraphicsContext gc;
    private final VBox rightColumn;
    private final VBox leftColumn;
    private final ObservableList<String> moveHistoryData;
    private final ListView<String> moveHistoryView;
    private final Label statusLabel;
    private final AnimationTimer animationTimer;
    private FlowPane whiteCapturedPane;
    private FlowPane blackCapturedPane;

    // Tracks which alliance is controlled by the human and whether the AI is mid-search.
    private Alliance humanAlliance = Alliance.WHITE;
    private boolean aiThinking;
    // Animation state for smooth piece travel
    private boolean animatingMove;
    private double animationProgress;
    private long animationStartNanos;
    private Piece animationPiece;
    private int animationSourceCoordinate = -1;
    private int animationDestinationCoordinate = -1;
    private double animationStartX;
    private double animationStartY;
    private double animationEndX;
    private double animationEndY;

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
    private ComboBox<Integer> aiDepthSelector;

    public GamePanel(final GameModel model) {
        this.model = model;
        this.boardCanvas = new Canvas(BOARD_PIXELS, BOARD_PIXELS);
        this.gc = boardCanvas.getGraphicsContext2D();
        this.moveHistoryData = FXCollections.observableArrayList();
        this.moveHistoryView = new ListView<>(moveHistoryData);
        this.statusLabel = new Label();
        this.rightColumn = buildRightColumn();
        this.leftColumn = buildLeftColumn();
        this.animationTimer = new AnimationTimer() {
            @Override
            public void handle(final long now) {
                if (!animatingMove) {
                    return;
                }
                final long elapsed = now - animationStartNanos;
                animationProgress = Math.min(1.0, elapsed / (double) MOVE_ANIMATION_DURATION_NANOS);
                redraw();
                if (animationProgress >= 1.0) {
                    completeAnimation();
                }
            }
        };

        initializePieceImages(); // Pre-load images to avoid IO jitter during painting
        setCenter(boardCanvas);
        setRight(rightColumn);
        setLeft(leftColumn);


        boardCanvas.setOnMousePressed(this::handleMousePressed);
        boardCanvas.setOnMouseDragged(this::handleMouseDragged);
        boardCanvas.setOnMouseReleased(this::handleMouseReleased);

        refreshMoveHistory();
        updateStatusLabel();
        updateCapturedDisplay();
        redraw();
    }
    /**
     * Builds the left-side information column showing captured pieces and other misc info.
     */
    private VBox buildLeftColumn() {
        final VBox box = new VBox(12);
        box.setAlignment(Pos.TOP_CENTER);
        box.setStyle("-fx-padding: 15; -fx-background-color: #f5f5f5;");

        final Label infoHeader = new Label("Extra Info");
        infoHeader.setStyle("-fx-font-weight: bold;");
        final Separator headerSeparator = new Separator();
        headerSeparator.setStyle("-fx-padding: 5 0 10 0;");

        final Label capturedHeader = new Label("Captured Pieces");
        capturedHeader.setStyle("-fx-font-weight: bold;");

        final Label whiteLabel = new Label("Captured White");
        whiteCapturedPane = createCapturedPane();

        final Label blackLabel = new Label("Captured Black");
        blackCapturedPane = createCapturedPane();

        box.getChildren().addAll(
                infoHeader,
                headerSeparator,
                capturedHeader,
                whiteLabel,
                whiteCapturedPane,
                blackLabel,
                blackCapturedPane
        );

        return box;
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

        final Label aiLabel = new Label("AI Difficulty (Depth)");
        aiLabel.setStyle("-fx-font-weight: bold;");
        aiDepthSelector = new ComboBox<>(FXCollections.observableArrayList(1, 2, 3, 4, 5));
        aiDepthSelector.setValue(model.getAiDepth());
        aiDepthSelector.setMaxWidth(Double.MAX_VALUE);
        aiDepthSelector.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                model.setAiDepth(newVal);
            }
        });
        final Button showTopMovesButton = new Button("Show Top Moves");
        showTopMovesButton.setMaxWidth(Double.MAX_VALUE);
        showTopMovesButton.setOnAction(e -> showTopMoves());
        box.getChildren().addAll(aiLabel, aiDepthSelector, showTopMovesButton);

        final Separator historySeparator = new Separator();
        historySeparator.setStyle("-fx-padding: 10 0 10 0;");
        box.getChildren().add(historySeparator);

        final Label historyLabel = new Label("Move History");
        historyLabel.setStyle("-fx-font-weight: bold;");
        box.getChildren().add(historyLabel);

        moveHistoryView.setPrefHeight(300);
        box.getChildren().add(moveHistoryView);
        VBox.setVgrow(moveHistoryView, Priority.NEVER);

        final Separator separatorButtons = new Separator();
        separatorButtons.setStyle("-fx-padding: 10 0 10 0;");
        box.getChildren().add(separatorButtons);

        final Button setupButton = new Button("Setup");
        setupButton.setMaxWidth(Double.MAX_VALUE);
        setupButton.setOnAction(e -> {
            if (aiThinking || animatingMove) {
                return;
            }
            showSetupDialog();
        });

        final Button undoButton = new Button("Undo");
        undoButton.setMaxWidth(Double.MAX_VALUE);
        undoButton.setOnAction(e -> {
            if (aiThinking || animatingMove) {
                return;
            }
            if (model.undo()) {
                while (model.getBoard().getCurrentPlayer().getAlliance() != humanAlliance && model.undo()) {
                    // continue undoing until it's the human's turn or history is exhausted
                }
                clearSelection();
                refreshMoveHistory();
                updateStatusLabel();
                updateCapturedDisplay();
                redraw();
            }
        });

        final Button restartButton = new Button("Restart");
        restartButton.setMaxWidth(Double.MAX_VALUE);
        restartButton.setOnAction(e -> {
            if (aiThinking || animatingMove) {
                return;
            }
            model.reset();
            clearSelection();
            setBoardFlipped(humanAlliance.isBlack());
            refreshMoveHistory();
            updateStatusLabel();
            updateCapturedDisplay();
            redraw();
            maybeRunAiMove();
        });

        box.getChildren().addAll(setupButton, undoButton, restartButton);
        return box;
    }

    private FlowPane createCapturedPane() {
        final FlowPane pane = new FlowPane();
        pane.setHgap(6);
        pane.setVgap(6);
        pane.setAlignment(Pos.CENTER);
        pane.setPrefWrapLength(120);
        return pane;
    }

    private void showSetupDialog() {
        if (aiThinking || animatingMove) {
            return;
        }
        final ChoiceDialog<String> dialog = new ChoiceDialog<>("White", List.of("White", "Black"));
        dialog.setTitle("Game Setup");
        dialog.setHeaderText(null);
        dialog.setContentText("Play as:");
        dialog.showAndWait().ifPresent(choice -> {
            final boolean playAsBlack = "Black".equalsIgnoreCase(choice);
            humanAlliance = playAsBlack ? Alliance.BLACK : Alliance.WHITE;
            setBoardFlipped(playAsBlack);
            clearSelection();
            refreshMoveHistory();
            updateStatusLabel();
            updateCapturedDisplay();
            redraw();
            maybeRunAiMove();
        });
    }

    private void showFenDialog() {
        if (aiThinking || animatingMove) {
            return;
        }
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
            setBoardFlipped(humanAlliance.isBlack());
            clearSelection();
            refreshMoveHistory();
            updateStatusLabel();
            updateCapturedDisplay();
            redraw();
            maybeRunAiMove();
        });
    }

    private void showPgnDialog() {
        if (aiThinking || animatingMove) {
            return;
        }
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
            setBoardFlipped(humanAlliance.isBlack());
            clearSelection();
            refreshMoveHistory();
            updateStatusLabel();
            updateCapturedDisplay();
            redraw();
            maybeRunAiMove();
        });
    }

    private void setBoardFlipped(final boolean flipped) {
        if (this.boardFlipped != flipped) {
            this.boardFlipped = flipped;
        }
    }

    private void handleMousePressed(final MouseEvent event) {
        if (aiThinking || animatingMove) {
            return;
        }
        final int coordinate = screenToCoordinate(event.getX(), event.getY());
        pressSelected = false;
        pressCoordinate = coordinate;
        dragActive = false;
        draggingPiece = null;
        draggingStartCoordinate = -1;
        pressWasSelected = selectedSquare == coordinate;

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
        if (aiThinking || animatingMove) {
            return;
        }
        if (draggingPiece == null || draggingStartCoordinate == -1) {
            return;
        }
        dragActive = true;
        dragX = event.getX() - dragOffsetX;
        dragY = event.getY() - dragOffsetY;
        redraw();
    }

    private void handleMouseReleased(final MouseEvent event) {
        if (aiThinking || animatingMove) {
            return;
        }
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
        if (aiThinking || animatingMove) {
            return;
        }
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
        if (aiThinking || animatingMove) {
            return false;
        }
        if (selectedSquare == -1) {
            return false;
        }
        final int origin = selectedSquare;
        GameModel.MoveAttemptResult result = model.makeMove(origin, destinationCoordinate);
        if (result.status() == GameModel.MoveAttemptStatus.PROMOTION_REQUIRED) {
            // Promotion requires user choice—prompt and retry with the selected piece.
            final PieceType choice = promptPromotion(result.promotionOptions());
            if (choice == null) {
                return false;
            }
            result = model.makeMove(origin, destinationCoordinate, choice);
        }
        if (result.status() != GameModel.MoveAttemptStatus.DONE) {
            return false;
        }
        handleSuccessfulMove(origin, destinationCoordinate);
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

    private void handleSuccessfulMove(final int origin, final int destination) {
        clearSelection();
        refreshMoveHistory();
        updateStatusLabel();
        updateCapturedDisplay();
        startMoveAnimation(origin, destination);
    }

    private void maybeRunAiMove() {
        if (aiThinking || animatingMove) {
            return;
        }
        final Player currentPlayer = model.getBoard().getCurrentPlayer();
        if (currentPlayer.getAlliance() == humanAlliance) {
            return;
        }
        if (currentPlayer.isInCheckMate() || currentPlayer.isInStaleMate() || currentPlayer.getLegalMoves().isEmpty()) {
            return;
        }

        final Optional<GameModel.ScoredMove> bookMove = model.getBookMove();
        if (bookMove.isPresent()) {
            executeAiMove(bookMove.get().move());
            return;
        }

        // Delegate computation to background thread so UI stays responsive.
        aiThinking = true;
        boardCanvas.setDisable(true);
        rightColumn.setDisable(true);
        leftColumn.setDisable(true);
        statusLabel.setText(currentPlayer.getAlliance() + " (AI) thinking...");

        final Task<GameModel.ScoredMove> task = new Task<>() {
            @Override
            protected GameModel.ScoredMove call() {
                final List<GameModel.ScoredMove> moves = model.getBestMoves(1);
                if (moves.isEmpty()) {
                    return null;
                }
                return moves.get(0);
            }
        };

        task.setOnSucceeded(event -> {
            aiThinking = false;
            boardCanvas.setDisable(false);
            rightColumn.setDisable(false);
            leftColumn.setDisable(false);
            final GameModel.ScoredMove scoredMove = task.getValue();
            if (scoredMove != null) {
                executeAiMove(scoredMove.move());
            } else {
                updateStatusLabel();
                redraw();
            }
        });

        task.setOnFailed(event -> {
            aiThinking = false;
            boardCanvas.setDisable(false);
            rightColumn.setDisable(false);
            leftColumn.setDisable(false);
            updateStatusLabel();
            updateCapturedDisplay();
            redraw();
            final Throwable exception = task.getException();
            if (exception != null) {
                exception.printStackTrace();
            }
        });

        final Thread thread = new Thread(task, "ai-move-worker");
        thread.setDaemon(true);
        thread.start();
    }

    private void startMoveAnimation(final int origin, final int destination) {
        animationTimer.stop();
        final Optional<Piece> maybePiece = model.getBoard().getPiece(destination);
        if (maybePiece.isEmpty()) {
            finishMoveWithoutAnimation();
            return;
        }
        final double[] start = coordinateToScreen(origin);
        final double[] end = coordinateToScreen(destination);
        if (origin == destination
                || (Math.abs(start[0] - end[0]) < 0.01 && Math.abs(start[1] - end[1]) < 0.01)) {
            finishMoveWithoutAnimation();
            return;
        }
        animationPiece = maybePiece.get();
        animationSourceCoordinate = origin;
        animationDestinationCoordinate = destination;
        animationStartX = start[0];
        animationStartY = start[1];
        animationEndX = end[0];
        animationEndY = end[1];
        animationProgress = 0.0;
        animationStartNanos = System.nanoTime();
        animatingMove = true;
        animationTimer.start();
        redraw();
    }

    private void finishMoveWithoutAnimation() {
        animationTimer.stop();
        animatingMove = false;
        animationPiece = null;
        animationSourceCoordinate = -1;
        animationDestinationCoordinate = -1;
        animationProgress = 1.0;
        redraw();
        maybeRunAiMove();
    }

    private void completeAnimation() {
        if (!animatingMove) {
            return;
        }
        animationTimer.stop();
        animatingMove = false;
        animationPiece = null;
        animationSourceCoordinate = -1;
        animationDestinationCoordinate = -1;
        animationProgress = 1.0;
        redraw();
        maybeRunAiMove();
    }

    private void executeAiMove(final Move move) {
        if (move == null) {
            updateStatusLabel();
            updateCapturedDisplay();
            redraw();
            return;
        }
        final int origin = move.getMovedPiece().getPiecePosition();
        final int destination = move.getDestinationCoordinate();
        final GameModel.MoveAttemptResult result = model.makeMove(move);
        if (result.status() == GameModel.MoveAttemptStatus.DONE) {
            handleSuccessfulMove(origin, destination);
        } else {
            updateStatusLabel();
            updateCapturedDisplay();
            redraw();
        }
    }

    private void showTopMoves() {
        if (aiThinking || animatingMove) {
            return;
        }
        final List<GameModel.ScoredMove> moves = model.getBestMoves(3);
        final Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("AI Suggestions");
        alert.setHeaderText("Top moves for " + model.getBoard().getCurrentPlayer().getAlliance());
        if (moves.isEmpty()) {
            alert.setContentText("No legal moves available.");
        } else {
            final StringBuilder builder = new StringBuilder();
            for (int i = 0; i < moves.size(); i++) {
                final GameModel.ScoredMove move = moves.get(i);
                builder.append(i + 1).append(". ").append(move.san());
                builder.append(" (").append(formatScore(move.score())).append(")");
                if (i + 1 < moves.size()) {
                    builder.append('\n');
                }
            }
            alert.setContentText(builder.toString());
        }
        alert.showAndWait();
    }

    private String formatScore(final int score) {
        if (Math.abs(score) >= 90_000) {
            return score > 0 ? "#+" : "#-";
        }
        return String.format("%+.2f", score / 100.0);
    }

    private PieceType promptPromotion(final List<PieceType> options) {
        if (options == null || options.isEmpty()) {
            return null;
        }
        final List<PieceType> choices = new ArrayList<>(options);
        final List<String> labels = choices.stream()
                .map(this::promotionLabel)
                .toList();
        final ChoiceDialog<String> dialog = new ChoiceDialog<>(labels.get(0), labels);
        dialog.setTitle("Pawn Promotion");
        dialog.setHeaderText("Choose promotion piece");
        dialog.setContentText("Promote to:");
        final Optional<String> selection = dialog.showAndWait();
        if (selection.isEmpty()) {
            return null;
        }
        final int index = labels.indexOf(selection.get());
        if (index < 0 || index >= choices.size()) {
            return null;
        }
        return choices.get(index);
    }

    private String promotionLabel(final PieceType type) {
        return switch (type) {
            case QUEEN -> "Queen";
            case ROOK -> "Rook";
            case BISHOP -> "Bishop";
            case KNIGHT -> "Knight";
            default -> type.toString();
        };
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
        final boolean animatePiece = animatingMove && animationPiece != null;
        for (int coordinate = 0; coordinate < BoardUtils.NUM_TILES; coordinate++) {
            final Optional<Piece> piece = model.getBoard().getPiece(coordinate);
            if (piece.isEmpty()) {
                continue;
            }
            if (animatePiece && coordinate == animationDestinationCoordinate) {
                // Skip drawing the piece at its destination; it will be drawn via animation.
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

        if (animatePiece) {
            final double currentX = animationStartX + (animationEndX - animationStartX) * animationProgress;
            final double currentY = animationStartY + (animationEndY - animationStartY) * animationProgress;
            final Image image = getImageForPiece(animationPiece);
            gc.drawImage(image, currentX, currentY, TILE_SIZE, TILE_SIZE);
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

    private void updateCapturedDisplay() {
        if (whiteCapturedPane == null || blackCapturedPane == null) {
            return;
        }
        final GameModel.CapturedPieces captured = model.getCapturedPieces();
        populateCapturedPane(whiteCapturedPane, captured.whiteCaptured(), Alliance.WHITE);
        populateCapturedPane(blackCapturedPane, captured.blackCaptured(), Alliance.BLACK);
    }

    private void populateCapturedPane(final FlowPane pane,
                                      final List<PieceType> capturedPieces,
                                      final Alliance alliance) {
        pane.getChildren().clear();
        if (capturedPieces == null || capturedPieces.isEmpty()) {
            return;
        }
        for (final PieceType type : capturedPieces) {
            final Image image = getImageForPiece(alliance, type);
            final ImageView view = new ImageView(image);
            view.setFitWidth(36);
            view.setFitHeight(36);
            view.setPreserveRatio(true);
            pane.getChildren().add(view);
        }
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
        return getImageForPiece(piece.getPieceAlliance(), piece.getPieceType());
    }

    private Image getImageForPiece(final Alliance alliance, final Piece.PieceType pieceType) {
        final Map<Piece.PieceType, Image> cacheForAlliance = imageCache.computeIfAbsent(
                alliance,
                key -> new EnumMap<>(Piece.PieceType.class));
        return cacheForAlliance.computeIfAbsent(pieceType, type -> loadImage(alliance, type));
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
