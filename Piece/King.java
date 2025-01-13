package Piece;

import java.io.FileNotFoundException;

public class King extends Piece {
    private final String name = "King";
    public King(int row, int col, int color) throws FileNotFoundException {
        super(row, col, color);
        if (color == 0) {
            this.image = getURL("/Users/tony/Documents/McGill/W2025/Chess/res/pieces-basic-png/black-king.png");
        }else{
            this.image = getURL("/Users/tony/Documents/McGill/W2025/Chess/res/pieces-basic-png/white-king.png");
        }
        System.out.println();
    }
    @Override
    public String getName() {
        return name;
    }

}
