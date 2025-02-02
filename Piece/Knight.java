package Piece;

import Main.Move;

import java.io.FileNotFoundException;
import java.util.ArrayList;

public class Knight extends Piece {
    private final String name = "Knight";
    private ArrayList <Move> moves = new ArrayList<>();
    public Knight(int row, int col, Boolean isWhite) throws FileNotFoundException {
        super(row, col, isWhite);
        calculateMoves();
//        for (Move move : moves) {
//            System.out.println("move : " + move.getRow() + " " + move.getCol());
//        }
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

    @Override
    public boolean canMove(int TargetRow, int TargetCol) {
        for (Move move : moves) {
            if (move.getRow() == TargetRow && move.getCol() == TargetCol) {
                return true;
            }
        }
        return false;}

    @Override
    public void calculateMoves() {
        int currentRow = this.getRow();
        int currentCol = this.getCol();
        int[][] directions = {{2, 1}, {2, -1}, {-2, 1}, {-2, -1}, {1, 2}, {1, -2}, {-1, 2}, {-1, -2}};
        for (int[] direction : directions) {
            int newRow = currentRow + direction[0];
            int newCol = currentCol + direction[1];
            if (newRow >= 0 && newRow < 8 && newCol >= 0 && newCol < 8) {
                moves.add(new Move(newRow, newCol));
            }

    }

}
}
