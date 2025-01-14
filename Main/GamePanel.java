package Main;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.awt.event.MouseAdapter;

import java.awt.event.MouseMotionListener;
import java.io.FileNotFoundException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

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

    public GamePanel(Board GameBoard) throws FileNotFoundException {
        this.GameBoard =GameBoard;
        this.activePieces = GameBoard.getActivePieces();
        this.capturedPiece = GameBoard.getCapturedPiece();
        for(int i = 0; i < 5; i++) {
            capturedPiece.add(new Knight(0, i, true));
        }
        capturedPiece.add(new Rook(0, 0, true));
        capturedPiece.add(new King(0, 0, true));
        capturedPiece.add(new Pawn(0, 0, true));
        for(int i = 0; i < 5; i++) {
            capturedPiece.add(new Knight(0, i, false));
        }
        capturedPiece.add(new Rook(0, 0, false));
        capturedPiece.add(new King(0, 0, false));
        capturedPiece.add(new Pawn(0, 0, false));

        capturedGrid = new GridPane();
        this.add(capturedGrid, 1, 0);




        Boardcanvas = new Canvas(800, 800);
        Capturedcanvas = new Canvas(300, 800);
        gc = Boardcanvas.getGraphicsContext2D();
        capturedGc = Capturedcanvas.getGraphicsContext2D();
        this.add(Boardcanvas, 0, 0);
        this.add(Capturedcanvas, 1, 0);
        drawCapturedPieces();

        mouse = new Mouse(GameBoard);
        Boardcanvas.setOnMousePressed(e -> {
            mouse.mousePressed(e);
        });
        Boardcanvas.setOnMouseDragged(e -> {
            mouse.mouseDragged(e);
        });

        Boardcanvas.setOnMouseReleased(e -> {
            mouse.mouseReleased(e);
            GameBoard.printBoard();

        });


    }



    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
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
//        capturedGrid.getChildren().clear(); // Clear the grid before redrawing
//        int blackRow = 0;
//        int blackCol = 0;
//        int whiteRow = 0;
//        int whiteCol = 0;
//
//        for (Piece p : capturedPiece) {
//            Canvas pieceCanvas = new Canvas(tilesize / 2, tilesize / 2);
//            GraphicsContext pieceGc = pieceCanvas.getGraphicsContext2D();
//            pieceGc.drawImage(p.getImage(), 0, 0, tilesize / 2, tilesize / 2);
//
//            if (p.isWhite()) {
//                capturedGrid.add(pieceCanvas, blackCol, blackRow);
//                blackCol++;
//                if (blackCol * (tilesize / 2) >= 200) { // Check if the column exceeds the width
//                    blackCol = 0;
//                    blackRow++;
//                }
//            } else {
//                capturedGrid.add(pieceCanvas, whiteCol, whiteRow + 10); // Offset for black pieces
//                whiteCol++;
//                if (whiteCol * (tilesize / 2) >= 200) { // Check if the column exceeds the width
//                    whiteCol = 0;
//                    whiteRow++;
//                }
//            }
//        }
//
//    }

    private void drawPieces(){
        Piece [][] board = GameBoard.getPieces();
//        System.out.println(Arrays.deepToString(board));
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (board[i][j] != null) {
                    gc.drawImage(board[i][j].getImage(), board[i][j].getX(), board[i][j].getY(), tilesize, tilesize);
                }
            }
        }

    }
    public void update () {
      //  System.out.println("update called");
        drawBoard();
        drawPieces();
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
