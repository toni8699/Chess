package Piece;

import java.io.FileNotFoundException;

public class Queen extends Piece {
    private final String name = "Queen";
    public Queen(int row, int col, Boolean isWhite) throws FileNotFoundException {
        super(row, col, isWhite);
        if (!isWhite) {
            this.image = getURL("/Users/tony/Documents/McGill/W2025/Chess/res/pieces-basic-png/black-queen.png");
        }else{
            this.image = getURL("/Users/tony/Documents/McGill/W2025/Chess/res/pieces-basic-png/white-queen.png");
        }
    }
    @Override
    public String getName() {
        return name;
    }
}
