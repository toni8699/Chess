package Piece;

import Main.Move;

import java.io.FileNotFoundException;
import java.util.ArrayList;

public class Queen extends Piece {
    private final String name = "Queen";
    private ArrayList <Move> moves = new ArrayList<>();
    public Queen(int row, int col, Boolean isWhite) throws FileNotFoundException {
        super(row, col, isWhite);
        calculateMoves();
        for (Move move : moves) {
            System.out.println("Queen moves:");
            System.out.println("move : " + move.getRow() + " " + move.getCol());
        }
        if (!isWhite) {
            this.image = getURL("/Users/tony/Documents/McGill/W2025/Chess/res/pieces-basic-png/black-queen.png");
        }else{
            this.image = getURL("/Users/tony/Documents/McGill/W2025/Chess/res/pieces-basic-png/white-queen.png");
        }
    }
    @ Override
    public boolean canMove(int TargetRow, int TargetCol) {
        for (Move move : moves) {
            if (move.getRow() == TargetRow && move.getCol() == TargetCol) {
                return true;
            }
        }
        return false;
    }
    @Override
    public String getName() {
        return name;
    }
    public void calculateMoves() {
        int currentRow = this.getRow();
        int currentCol = this.getCol();
        for (int i =-1 ;i<=1 ; i++) {
            for (int j =-1 ;j<=1 ; j++) {
                if (i == 0 && j == 0) continue; // skip the current position
                int newRow = currentRow + i;
                int newCol = currentCol + j;
                while (newRow >= 0 && newRow <= 7 && newCol >= 0 && newCol <= 7) {
                    Move move = new Move(newRow, newCol);
                    moves.add(move);
                    newRow += i;
                    newCol += j;
                }
            }
        }


    }
}
