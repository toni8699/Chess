package Piece;

import java.io.FileNotFoundException;
import java.util.ArrayList;

public class King extends Piece {
    private ArrayList<Piece> capturedPiece = new ArrayList<>();
    private final String name = "King";
    public King(int row, int col, Boolean isWhite) throws FileNotFoundException {
        super(row, col, isWhite);;
        if (!isWhite) {
            this.image = getURL("/Users/tony/Documents/McGill/W2025/Chess/res/pieces-basic-png/black-king.png");
        }else{
            this.image = getURL("/Users/tony/Documents/McGill/W2025/Chess/res/pieces-basic-png/white-king.png");
        }

    }
    @Override
    public String getName() {
        return name;
    }
    @Override
    public boolean canMove(int row, int col) {
        if (Math.abs(row - this.getRow()) > 1 || Math.abs(col - this.getCol()) > 1) {
            return false;
        }


        return true;
    }

}
