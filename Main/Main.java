package Main;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ui.GameModel;
import ui.GamePanel;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        final GameModel model = new GameModel();
        final GamePanel panel = new GamePanel(model);
        final Scene scene = new Scene(panel);
        stage.setTitle("Chess");
        stage.setScene(scene);
        stage.show();
    }
}
