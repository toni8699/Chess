package Main;

import javafx.scene.layout.Pane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class GamePanel extends Pane implements Runnable {
    private final int FPS = 60;
    Thread gameThread;
    Board board = new Board();

    public GamePanel() {
        board.drawBoard(this);
    }

    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }
    private void update() {

    }

    @Override
    public void run() {
        //Game loop
        double drawInterval = 1000000000 / FPS;
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
}
