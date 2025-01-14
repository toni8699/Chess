package Piece;

import java.io.FileNotFoundException;

public class Bishop extends Piece{
    private final String name = "Bishop";
    public Bishop(int row, int col, Boolean isWhite) throws FileNotFoundException {
        super(row, col, isWhite);
        if (!isWhite ) {
            this.image = getURL("/Users/tony/Documents/McGill/W2025/Chess/res/pieces-basic-png/black-bishop.png");
        }else{
            this.image = getURL("/Users/tony/Documents/McGill/W2025/Chess/res/pieces-basic-png/white-bishop.png");
        }
        System.out.println();
    }
    @Override
    public String getName() {
        return name;
    }
}
