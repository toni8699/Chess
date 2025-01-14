package Piece;

import java.io.FileNotFoundException;

public class Knight extends Piece {
    private final String name = "Knight";
    public Knight(int row, int col, Boolean isWhite) throws FileNotFoundException {
        super(row, col, isWhite);
        if (!isWhite) {
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
