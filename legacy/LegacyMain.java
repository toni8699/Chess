package legacy;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class LegacyMain extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        Board board = new Board();
        GamePanel panel = new GamePanel(board);
        Scene scene = new Scene(panel);
        stage.setScene(scene);
        stage.show();

        panel.startGameThread();
    }
}
