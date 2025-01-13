package Piece;

import java.io.FileNotFoundException;

public class Rook extends Piece {
    private final String name = "Rook";
    public Rook(int row, int col, int color) throws FileNotFoundException, FileNotFoundException {
        super(row, col, color);
        if (color == 0) {
            this.image = getURL("/Users/tony/Documents/McGill/W2025/Chess/res/pieces-basic-png/black-rook.png");
        }else{
            this.image = getURL("/Users/tony/Documents/McGill/W2025/Chess/res/pieces-basic-png/white-rook.png");
        }
        System.out.println();
    }
    @Override
    public String getName() {
        return name;
    }

}
