package Piece;

import Main.Move;

import java.io.FileNotFoundException;
import java.util.ArrayList;

public class Bishop extends Piece{
    private final String name = "Bishop";
    private ArrayList <Move> moves = new ArrayList<>();
    public Bishop(int row, int col, Boolean isWhite) throws FileNotFoundException {
        super(row, col, isWhite);
        calculateMoves();
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
    @Override
    public boolean canMove(int TargetRow, int TargetCol) {
        for (Move move : this.moves) {
            if (move.getRow() == TargetRow && move.getCol() == TargetCol) {
                return true;
            }
        }
        return false;
    }
    @Override
    public  void calculateMoves() {
        int currentRow = this.getRow();
        int currentCol = this.getCol();

        // Iterate over the possible diagonal directions
        for (int i = -1; i <= 1; i += 2) {
            for (int j = -1; j <= 1; j += 2) {
                int newRow = currentRow + i;
                int newCol = currentCol + j;

                // Continue iterating in the diagonal direction until the edge of the board is reached
                while (newRow >= 0 && newRow < 8 && newCol >= 0 && newCol < 8) {
                    moves.add(new Move(newRow, newCol));
                    newRow += i;
                    newCol += j;
                }
            }
        }
    }
}
