package Piece;

import Main.Move;

import java.io.FileNotFoundException;
import java.util.ArrayList;

public class Rook extends Piece {
    private final String name = "Rook";
    private ArrayList <Move> moves = new ArrayList<>();
    public Rook(int row, int col, Boolean isWhite) throws FileNotFoundException, FileNotFoundException {
            super(row, col, isWhite);
            calculateMoves();
        if (!isWhite) {
            this.image = getURL("/Users/tony/Documents/McGill/W2025/Chess/res/pieces-basic-png/black-rook.png");
        }else{
            this.image = getURL("/Users/tony/Documents/McGill/W2025/Chess/res/pieces-basic-png/white-rook.png");
        }
        System.out.println();
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
        return false;
    }
    public void calculateMoves() {
        for (int i = this.getRow(); i < 8; i++) {
            moves.add(new Move(i, this.getCol()));
        }
        for (int i = this.getRow(); i > -1; i--) {
            moves.add(new Move(i, this.getCol()));
        }
        for (int i = this.getCol(); i < 8; i++) {
            moves.add(new Move(this.getRow(), i));
        }
        for (int i = this.getCol(); i > -1; i--) {
            moves.add(new Move(this.getRow(), i));
        }
    }

}
