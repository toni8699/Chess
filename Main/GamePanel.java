package Main;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import Piece.*;
import Piece.King;

public class GamePanel extends GridPane implements Runnable {
    private final int FPS = 60;
    Thread gameThread;
    private int tilesize = 100;
    private Board GameBoard;
    private Canvas Boardcanvas;
    private Canvas Capturedcanvas;
    private ArrayList <Piece> activePieces = new ArrayList<>();
    private GraphicsContext gc;
    private GraphicsContext capturedGc;
    private Mouse mouse;
    private Piece selectedPiece ;
    private ArrayList <Piece> capturedPiece;
    private GridPane capturedGrid;

    public GamePanel(Board GameBoard) throws FileNotFoundException {
        this.GameBoard =GameBoard;
        this.activePieces = GameBoard.getActivePieces();
        this.capturedPiece = GameBoard.getCapturedPiece();
        capturedGrid = new GridPane();
        this.add(capturedGrid, 1, 0);
        Boardcanvas = new Canvas(800, 800);
        Capturedcanvas = new Canvas(300, 800);
        gc = Boardcanvas.getGraphicsContext2D();
        capturedGc = Capturedcanvas.getGraphicsContext2D();
        this.add(Boardcanvas, 0, 0);
        this.add(Capturedcanvas, 1, 0);

//        capturedPiece.add(new King(0, 0, true,GameBoard));

        drawCapturedPieces();

        mouse = new Mouse(GameBoard,this);
        Boardcanvas.setOnMousePressed(e -> {
            mouse.mousePressed(e);
            drawHighlightedMoves(GameBoard.getSelectedPiece());

        });
        Boardcanvas.setOnMouseDragged(e -> {
            mouse.mouseDragged(e);
        });

        Boardcanvas.setOnMouseReleased(e -> {
            try {
                mouse.mouseReleased(e);
            } catch (FileNotFoundException ex) {
                throw new RuntimeException(ex);
            }
            GameBoard.printBoard();
        });




    }



    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    public void drawHighlightedMoves(Piece selectedPiece) {
        if (selectedPiece != null) {
//            System.out.println("Drawing highlighted moves for " + selectedPiece.getName());
            for (Move move : selectedPiece.getMoves()) {
//                System.out.println("drawing move" + move.getRow() + " " + move.getCol());
                gc.setFill(Color.color(1, 1, 0, 0.5)); // RGBA values0.0 = fully transparent, 1.0 = fully opaque

                gc.fillRect(move.getCol() * tilesize, move.getRow() * tilesize, tilesize, tilesize);
            }
        }
    }


    private void drawBoard() {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if ((i + j) % 2 == 0) {
                    gc.setFill(Color.WHITESMOKE);
                } else {
                    gc.setFill(Color.DARKOLIVEGREEN);
                }
                gc.fillRect(i * tilesize, j * tilesize, tilesize, tilesize);
            }
        }
        // Draw a thick border around the board
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.strokeRect(0, 0, 8 * tilesize, 8 * tilesize);

        // Draw coordinates 1-8 and a-h
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font(24));
        for (int i = 0; i <=8; i++) {
            gc.fillText(String.valueOf(8 - i), 10, (i + 1) * tilesize - 10);
            gc.fillText(String.valueOf((char) ('a' + i)), (i + 1) * tilesize - 20, 10);
        }
    }
    private void drawCapturedPieces() {
        // Clear the captured pieces canvas first
        capturedGc.clearRect(0, 0, 300, 800);
        
        int blackCapturedX = 0;
        int blackCapturedY = 500;
        int whiteCapturedX = 0;
        int whiteCapturedY = 0;
        int size = 75;
        int maxWidth = 200; // Width of the captured canvas
        
        // Refresh captured pieces list
        this.capturedPiece = GameBoard.getCapturedPiece();
        
        for (Piece p : capturedPiece) {
            if (p.isWhite()) {
                capturedGc.drawImage(p.getImage(), whiteCapturedX, whiteCapturedY, size, size);
                whiteCapturedX += 40;
                if (whiteCapturedX >= maxWidth) {
                    whiteCapturedX = 0;
                    whiteCapturedY +=100;
                }
            }else{
                capturedGc.drawImage(p.getImage(), blackCapturedX, blackCapturedY, size, size);
                blackCapturedX +=40;
            } if (blackCapturedX >= maxWidth) {
                blackCapturedX = 0;
                blackCapturedY +=100;

            }

        }}

    private void drawPieces(){
        Piece [][] board = GameBoard.getPieces();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (board[i][j] != null) {
                    Image pieceImage = board[i][j].getImage();
                    if (pieceImage != null) {
                        try {
                            gc.drawImage(pieceImage, board[i][j].getX(), board[i][j].getY(), tilesize, tilesize);
                        } catch (Exception e) {
                            System.err.println("Error drawing piece at [" + i + "][" + j + "]: " + e.getMessage());
                            // Draw a placeholder
                            gc.setFill(javafx.scene.paint.Color.RED);
                            gc.fillRect(board[i][j].getX(), board[i][j].getY(), tilesize, tilesize);
                        }
                    }
                }
            }
        }
    }
    public void update () {
        drawBoard();
        drawPieces();
        drawCheckIndicator();
        drawGameStatus();
        drawCapturedPieces();
        drawHighlightedMoves(GameBoard.getSelectedPiece());
    }
    
    private void drawCheckIndicator() {
        Board.GameState state = GameBoard.getGameState();
        if (state == Board.GameState.CHECK || state == Board.GameState.WHITE_CHECKMATE || state == Board.GameState.BLACK_CHECKMATE) {
            // Highlight the king in check
            Piece king = null;
            boolean checkWhite = false;
            
            if (state == Board.GameState.CHECK) {
                // Determine which player is in check based on whose turn it is
                checkWhite = GameBoard.isWhiteTurn();
            } else if (state == Board.GameState.WHITE_CHECKMATE) {
                checkWhite = true;
            } else if (state == Board.GameState.BLACK_CHECKMATE) {
                checkWhite = false;
            }
            
            // Find the king in check
            for (Piece p : GameBoard.getActivePieces()) {
                if (p instanceof King && p.isWhite() == checkWhite) {
                    king = p;
                    break;
                }
            }
            
            if (king != null) {
                gc.setFill(Color.color(1, 0, 0, 0.4)); // Red highlight with transparency
                gc.fillRect(king.getCol() * tilesize, king.getRow() * tilesize, tilesize, tilesize);
            }
        }
    }
    
    private void drawGameStatus() {
        Board.GameState state = GameBoard.getGameState();
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font(20));
        
        String statusText = "";
        String turnText = "Turn: " + (GameBoard.isWhiteTurn() ? "White" : "Black");
        
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
        }
        
        // Draw status at the bottom of the board
        gc.fillText(statusText, 10, 800 - 10);
    }



    public void run() {
        //Game loop
        double drawInterval = (double) 1000000000 / FPS;
        long lastTime = System.nanoTime();
        long currentTime;
        long timer = 0;
        while (gameThread != null) {
            currentTime = System.nanoTime();
            timer += currentTime - lastTime;
            lastTime = currentTime;
            if (timer > drawInterval) {
                //Draw here
                update();
                timer = 0;
            }
        }

    }
    private void copyBoard(Piece[][] board) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (board[i][j] != null) {
                    activePieces.add(board[i][j]);
                }
            }
        }


    }

}
