package Main;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
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

public class GamePanel extends Pane implements Runnable {
    private final int FPS = 60;
    Thread gameThread;
    private int tilesize = 100;
    private Board GameBoard;
    private Canvas canvas;
    private ArrayList <Piece> activePieces = new ArrayList<>();
    private GraphicsContext gc;
    private Mouse mouse;
    private Piece selectedPiece ;

    public GamePanel(Board GameBoard) throws FileNotFoundException {
        this.GameBoard =GameBoard;
        this.activePieces = GameBoard.getActivePieces();
        canvas = new Canvas(800, 800);
        this.getChildren().add(canvas);
        gc = canvas.getGraphicsContext2D();
        copyBoard(GameBoard.getPieces());

//        GameBoard.printBoard();
//        mouse = new Mouse(GameBoard);
//
//        Piece p= GameBoard.getPiece(0, 0);
//        GameBoard.movePiece(0,2, p);
//        System.out.println( p.getX()/100 + " " + p.getY()/100 );
//        GameBoard.printBoard();
//
//        update();
//        GameBoard.capture(p);
        GameBoard.printBoard();
        mouse = new Mouse(GameBoard);
        canvas.setOnMousePressed(e -> {
            mouse.mousePressed(e);
        });
        canvas.setOnMouseDragged(e -> {
            mouse.mouseDragged(e);
        });

        canvas.setOnMouseReleased(e -> {
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

    private void drawPieces( ){
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
