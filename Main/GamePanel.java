package Main;

import Piece.King;
import Piece.Piece;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;

public class GamePanel extends BorderPane implements Runnable {
    private final int FPS = 60;
    private Thread gameThread;
    private final int tileSize = 100;
    private final Board board;
    private final Canvas boardCanvas;
    private final Canvas capturedCanvas;
    private final GraphicsContext gc;
    private final GraphicsContext capturedGc;
    private final Mouse mouse;
    private final VBox rightColumn;
    private final ObservableList<String> moveHistoryData;
    private final ListView<String> moveHistoryView;
    private boolean boardFlipped = false;

    public GamePanel(Board board) throws FileNotFoundException {
        this.board = board;

        boardCanvas = new Canvas(800, 800);
        gc = boardCanvas.getGraphicsContext2D();

        mouse = new Mouse(board, this);
        boardCanvas.setOnMousePressed(e -> {
            mouse.mousePressed(e);
            drawHighlightedMoves(board.getSelectedPiece());
        });
        boardCanvas.setOnMouseDragged(mouse::mouseDragged);
        boardCanvas.setOnMouseReleased(e -> {
            try {
                mouse.mouseReleased(e);
            } catch (FileNotFoundException ex) {
                throw new RuntimeException(ex);
            }
            board.printBoard();
        });

        setCenter(boardCanvas);

        capturedCanvas = new Canvas(300, 600);
        capturedGc = capturedCanvas.getGraphicsContext2D();

        rightColumn = new VBox(10);
        rightColumn.setAlignment(Pos.TOP_CENTER);
        rightColumn.setStyle("-fx-padding: 15;");
        rightColumn.getChildren().add(capturedCanvas);
        VBox.setVgrow(capturedCanvas, Priority.NEVER);

        Separator separator = new Separator();
        separator.setStyle("-fx-padding: 10 0 10 0;");
        rightColumn.getChildren().add(separator);

        Label historyLabel = new Label("Move History");
        historyLabel.setStyle("-fx-font-weight: bold;");
        rightColumn.getChildren().add(historyLabel);

        moveHistoryData = FXCollections.observableArrayList();
        moveHistoryView = new ListView<>(moveHistoryData);
        moveHistoryView.setPrefHeight(300);
        rightColumn.getChildren().add(moveHistoryView);

        Separator separator2 = new Separator();
        separator2.setStyle("-fx-padding: 10 0 10 0;");
        rightColumn.getChildren().add(separator2);

        Button setupButton = new Button("Setup");
        setupButton.setMaxWidth(Double.MAX_VALUE);
        setupButton.setOnAction(event -> showSetupDialog());

        Button undoButton = new Button("Undo");
        undoButton.setMaxWidth(Double.MAX_VALUE);
        undoButton.setOnAction(event -> {
            try {
                if (board.undo()) {
                    alignPiecesToBoard();
                    update();
                }
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        });

        Button restartButton = new Button("Restart");
        restartButton.setMaxWidth(Double.MAX_VALUE);
        restartButton.setOnAction(event -> {
            try {
                board.reset();
                alignPiecesToBoard();
                update();
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        });

        rightColumn.getChildren().addAll(setupButton, undoButton, restartButton);
        setRight(rightColumn);

        alignPiecesToBoard();
        drawCapturedPieces();
        moveHistoryData.setAll(board.getMoveHistory());
    }

    private void showSetupDialog() {
        ChoiceDialog<String> dialog = new ChoiceDialog<>("White", Arrays.asList("White", "Black"));
        dialog.setTitle("Game Setup");
        dialog.setHeaderText(null);
        dialog.setContentText("Play as:");
        dialog.showAndWait().ifPresent(choice -> {
            setBoardFlipped("Black".equalsIgnoreCase(choice));
            alignPiecesToBoard();
            update();
        });
    }

    private void setBoardFlipped(boolean flipped) {
        if (this.boardFlipped != flipped) {
            this.boardFlipped = flipped;
        }
    }

    public double tileToScreenCol(int boardCol) {
        int col = boardFlipped ? 7 - boardCol : boardCol;
        return col * tileSize;
    }

    public double tileToScreenRow(int boardRow) {
        int row = boardFlipped ? 7 - boardRow : boardRow;
        return row * tileSize;
    }

    public double getTileSize() {
        return tileSize;
    }

    public int screenToBoardCol(double screenX) {
        int col = clamp(0, 7, (int) (screenX / tileSize));
        return boardFlipped ? 7 - col : col;
    }

    public int screenToBoardRow(double screenY) {
        int row = clamp(0, 7, (int) (screenY / tileSize));
        return boardFlipped ? 7 - row : row;
    }

    private int clamp(int min, int max, int value) {
        return Math.max(min, Math.min(max, value));
    }

    public void alignPiecesToBoard() {
        Piece selected = board.getSelectedPiece();
        Piece[][] pieces = board.getPieces();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = pieces[row][col];
                if (piece != null && piece != selected) {
                    piece.setX(tileToScreenCol(col));
                    piece.setY(tileToScreenRow(row));
                }
            }
        }
    }

    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    public void drawHighlightedMoves(Piece selectedPiece) {
        if (selectedPiece != null) {
            for (Move move : selectedPiece.getMoves()) {
                gc.setFill(Color.color(1, 1, 0, 0.5));
                gc.fillRect(tileToScreenCol(move.getCol()), tileToScreenRow(move.getRow()), tileSize, tileSize);
            }
        }
    }

    private void drawBoard() {
        for (int col = 0; col < 8; col++) {
            for (int row = 0; row < 8; row++) {
                gc.setFill((col + row) % 2 == 0 ? Color.WHITESMOKE : Color.DARKOLIVEGREEN);
                gc.fillRect(tileToScreenCol(col), tileToScreenRow(row), tileSize, tileSize);
            }
        }
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.strokeRect(0, 0, 8 * tileSize, 8 * tileSize);

        gc.setFill(Color.BLACK);
        gc.setFont(Font.font(24));
        for (int i = 0; i <= 8; i++) {
            gc.fillText(String.valueOf(8 - i), 10, (i + 1) * tileSize - 10);
            gc.fillText(String.valueOf((char) ('a' + i)), (i + 1) * tileSize - 20, 10);
        }
    }

    private void drawCapturedPieces() {
        capturedGc.clearRect(0, 0, capturedCanvas.getWidth(), capturedCanvas.getHeight());

        ArrayList<Piece> capturedPieces = board.getCapturedPiece();
        int blackCapturedX = 0;
        int blackCapturedY = 300;
        int whiteCapturedX = 0;
        int whiteCapturedY = 0;
        int size = 75;
        int maxWidth = 200;

        for (Piece p : capturedPieces) {
            if (p.isWhite()) {
                capturedGc.drawImage(p.getImage(), whiteCapturedX, whiteCapturedY, size, size);
                whiteCapturedX += 40;
                if (whiteCapturedX >= maxWidth) {
                    whiteCapturedX = 0;
                    whiteCapturedY += 100;
                }
            } else {
                capturedGc.drawImage(p.getImage(), blackCapturedX, blackCapturedY, size, size);
                blackCapturedX += 40;
                if (blackCapturedX >= maxWidth) {
                    blackCapturedX = 0;
                    blackCapturedY += 100;
                }
            }
        }
    }

    private void drawPieces() {
        Piece selected = board.getSelectedPiece();
        Piece[][] pieces = board.getPieces();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = pieces[row][col];
                if (piece != null) {
                    Image pieceImage = piece.getImage();
                    if (pieceImage != null) {
                        try {
                            double drawX;
                            double drawY;
                            if (piece == selected) {
                                drawX = piece.getX();
                                drawY = piece.getY();
                            } else {
                                drawX = tileToScreenCol(col);
                                drawY = tileToScreenRow(row);
                                piece.setX(drawX);
                                piece.setY(drawY);
                            }
                            gc.drawImage(pieceImage, drawX, drawY, tileSize, tileSize);
                        } catch (Exception e) {
                            gc.setFill(Color.RED);
                            gc.fillRect(tileToScreenCol(col), tileToScreenRow(row), tileSize, tileSize);
                        }
                    }
                }
            }
        }
    }

    public void update() {
        alignPiecesToBoard();
        drawBoard();
        drawPieces();
        drawCheckIndicator();
        drawGameStatus();
        drawCapturedPieces();
        drawHighlightedMoves(board.getSelectedPiece());
        moveHistoryData.setAll(board.getMoveHistory());
    }

    private void drawCheckIndicator() {
        Board.GameState state = board.getGameState();
        if (state == Board.GameState.CHECK || state == Board.GameState.WHITE_CHECKMATE || state == Board.GameState.BLACK_CHECKMATE) {
            Piece king = null;
            boolean checkWhite;

            if (state == Board.GameState.CHECK) {
                checkWhite = board.isWhiteTurn();
            } else {
                checkWhite = state == Board.GameState.WHITE_CHECKMATE;
            }

            for (Piece p : board.getActivePieces()) {
                if (p instanceof King && p.isWhite() == checkWhite) {
                    king = p;
                    break;
                }
            }

            if (king != null) {
                gc.setFill(Color.color(1, 0, 0, 0.4));
                gc.fillRect(tileToScreenCol(king.getCol()), tileToScreenRow(king.getRow()), tileSize, tileSize);
            }
        }
    }

    private void drawGameStatus() {
        Board.GameState state = board.getGameState();
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font(20));

        String statusText;
        String turnText = "Turn: " + (board.isWhiteTurn() ? "White" : "Black");

        switch (state) {
            case PLAYING:
                statusText = turnText;
                break;
            case CHECK:
                statusText = turnText + " - CHECK!";
                gc.setFill(Color.RED);
                break;
            case WHITE_CHECKMATE:
                statusText = "WHITE CHECKMATE - Black Wins!";
                gc.setFill(Color.RED);
                break;
            case BLACK_CHECKMATE:
                statusText = "BLACK CHECKMATE - White Wins!";
                gc.setFill(Color.RED);
                break;
            case STALEMATE:
                statusText = "STALEMATE - Draw!";
                gc.setFill(Color.ORANGE);
                break;
            default:
                statusText = "";
        }

        gc.fillText(statusText, 10, 800 - 10);
    }

    @Override
    public void run() {
        double drawInterval = 1_000_000_000.0 / FPS;
        long lastTime = System.nanoTime();
        long currentTime;
        long timer = 0;
        while (gameThread != null) {
            currentTime = System.nanoTime();
            timer += currentTime - lastTime;
            lastTime = currentTime;
            if (timer > drawInterval) {
                Platform.runLater(this::update);
                timer = 0;
            }
        }
    }
}
