package Main;

import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import static javafx.application.Application.launch;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import javafx.application.Application;

import java.awt.*;

// Main class must extend Application
public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }
    @Override
    public void start(Stage stage) throws Exception {
        GamePanel board = new GamePanel();

        Scene scene = new Scene(board);
        stage.setScene(scene);

        stage.show();
        board.startGameThread();


    }
}