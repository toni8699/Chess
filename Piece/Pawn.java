package Piece;

import javafx.scene.image.Image;

import java.awt.*;
import java.io.FileNotFoundException;

public class Pawn extends Piece {
    private String name = "Pawn";
    public Pawn(int row, int col, int color) throws FileNotFoundException {
        super(row, col, color);
        if (color == 0) {

            this.image = getURL("/Users/tony/Documents/McGill/W2025/Chess/res/pieces-basic-png/black-pawn.png");
        }else{
            this.image = getURL("/Users/tony/Documents/McGill/W2025/Chess/res/pieces-basic-png/white-pawn.png");
        }
        System.out.println();
    }
    @Override
    public String getName() {
        return name;
    }



}
