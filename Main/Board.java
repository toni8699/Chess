package Main;

import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;

import java.awt.*;

public class Board {
    final int col = 8;
    final int row = 8;
    public static final int tilesize = 100;


    /**
     * Draw the chess board on the given pane.
     * @param pane pane to draw the chess board on
     */
    public void drawBoard(Pane pane) {
        // Iterate over all the squares on the board
        for (int i = 0; i < col; i++) {
            for (int j = 0; j < row; j++) {
                // Create a new square
                Rectangle rectangle = new Rectangle(i * tilesize, j * tilesize, tilesize, tilesize);
                // Alternate the color of the squares
                if ((i + j) % 2 == 0) {
                    rectangle.setFill(Color.WHITESMOKE);
                } else {
                    rectangle.setFill(Color.DARKOLIVEGREEN);
                }
                // Add the square to the pane
                pane.getChildren().add(rectangle);
            }
        }
    }


}

