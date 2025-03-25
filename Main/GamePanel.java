package Main;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.GridPane;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.io.FileNotFoundException;
import java.util.ArrayList;

import Piece.*;

import static com.sun.java.accessibility.util.AWTEventMonitor.addMouseListener;
import static com.sun.java.accessibility.util.AWTEventMonitor.addMouseMotionListener;

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
    private Button restartButton;

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
        restartButton = new Button("Restart");
//        restartButton.setLayoutX(100);
//        restartButton.setLayoutY(100);
//        this.add(restartButton, 1, 0);
        this.add(restartButton ,1,0);
        restartButton.setOnAction(e -> {
            try {
                restartGame();
            } catch (FileNotFoundException ex) {
                throw new RuntimeException(ex);
            }
        });

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
    private void restartGame() throws FileNotFoundException {
        GameBoard.resetBoard();
        activePieces = GameBoard.getActivePieces();
        capturedPiece = GameBoard.getCapturedPiece();
        update();
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
        int blackCapturedX = 0;
        int blackCapturedY = 500;
        int whiteCapturedX = 0;
        int whiteCapturedY = 0;
        int size = 75;
        int maxWidth = 200; // Width of the captured canvas
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
//        System.out.println("being called");
        Piece [][] board = GameBoard.getPieces();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (board[i][j] != null) {
                    gc.drawImage(board[i][j].getImage(), board[i][j].getX(), board[i][j].getY(), tilesize, tilesize);
                }
            }
        }

    }
    public void update () {
        drawBoard();
        drawPieces();
        drawCapturedPieces();
        drawHighlightedMoves(GameBoard.getSelectedPiece());

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
