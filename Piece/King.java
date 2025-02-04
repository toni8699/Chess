package Piece;

import Main.Board;
import Main.Move;

import java.io.FileNotFoundException;
import java.util.ArrayList;

public class King extends Piece {
    private ArrayList<Piece> capturedPiece = new ArrayList<>();
    private ArrayList <Move> moves = new ArrayList<>();
    private final String name = "King";
    private Board board;
    public King(int row, int col, Boolean isWhite, Board board) throws FileNotFoundException {
        super(row, col, isWhite,board);
        this.board = board;
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
    public ArrayList<Move> getMoves() {
        return moves;
    }



    @Override
    public void calculateMoves() {
        moves = new ArrayList<>();
        int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {-1, -1}, {1, -1}, {-1, 1}};
        for (int[] direction : directions) {
            int newRow = this.getRow() + direction[0];
            int newCol = this.getCol() + direction[1];

            if (newRow >= 0 && newRow < 8 && newCol >= 0 && newCol < 8) {
                if (board.isEmpty(newRow, newCol)) {
                    moves.add(new Move(newRow, newCol));
                } else {
                    if (board.getPiece(newRow, newCol).getColor() != this.getColor()) {
                        moves.add(new Move(newRow, newCol));
                    }
                }
            }
        }
    }

}
