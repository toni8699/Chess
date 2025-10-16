package Main;

import javafx.stage.Stage;
import static javafx.application.Application.launch;
import javafx.scene.Scene;
import javafx.application.Application;

// Main class must extend Application
public class Main extends Application {
    private static Stockfish stockfish;

    public static void main(String[] args) {
        launch(args);
    }
    @Override
    public void start(Stage stage) throws Exception {
        stockfish = new Stockfish();
        if (stockfish.startEngine()) {
            System.out.println("Stockfish engine started successfully.");
        } else {
            System.err.println("Failed to start Stockfish engine.");
        }

        Board board = new Board();
        GamePanel panel = new GamePanel(board);
        Scene scene = new Scene(panel);
        stage.setScene(scene);
        stage.show();

        panel.startGameThread();

        stage.setOnCloseRequest(e -> {
            stockfish.stopEngine();
            System.out.println("Stockfish engine stopped.");
        });
    }
}