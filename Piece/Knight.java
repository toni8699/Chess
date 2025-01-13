package Piece;

import java.io.FileNotFoundException;

public class Knight extends Piece {
    private final String name = "Knight";
    public Knight(int row, int col, int color) throws FileNotFoundException {
        super(row, col, color);
        if (color == 0) {
            this.image = getURL("/Users/tony/Documents/McGill/W2025/Chess/res/pieces-basic-png/black-knight.png");
        }else{
            this.image = getURL("/Users/tony/Documents/McGill/W2025/Chess/res/pieces-basic-png/white-knight.png");
        }
    }
    @Override
    public String getName() {
        return name;
    }

}
