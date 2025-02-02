package Piece;

import Main.Move;

import java.io.FileNotFoundException;
import java.util.ArrayList;

public class King extends Piece {
    private ArrayList<Piece> capturedPiece = new ArrayList<>();
    private ArrayList <Move> moves = new ArrayList<>();
    private final String name = "King";
    public King(int row, int col, Boolean isWhite) throws FileNotFoundException {
        super(row, col, isWhite);
        System.out.println(this.getColor()+ " " + this.getName()+ " " + this.getRow() + " " + this.getCol());
        calculateMoves();
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
    public boolean canMove(int TargetRow, int TargetCol) {
        for (Move move : moves) {
            if (move.getRow() == TargetRow && move.getCol() == TargetCol) {
                return true;
            }
        }
        System.out.println("can't move");
        return false;

    }


    @Override
    public void calculateMoves() {
        int currentRow = this.getRow();
        int currentCol = this.getCol();
        for (int i =-1 ;i<=1 ; i++) {
            for (int j =-1 ;j<=1 ; j++) {
                if (currentRow + i >= 0 && currentRow + i <= 7 && currentCol + j >= 0 && currentCol + j <= 7) {
                    Move move = new Move(currentRow + i, currentCol + j);
                    moves.add(move);
                }
            }
        }
    }

}
