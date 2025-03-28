package Piece;

import Main.Board;
import Main.Move;
import Main.Position;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Objects;

public class Bishop extends Piece{
    private final String name = "Bishop";

    private ArrayList <Move> moves = new ArrayList<>();
    public Bishop(int row, int col, Boolean isWhite, Board board) throws FileNotFoundException {
        super(row, col, isWhite,board);
        calculateMoves();
        if (!isWhite ) {
            this.image = getURL("/Users/tony/Documents/McGill/W2025/Chess/res/pieces-basic-png/black-bishop.png");
        }else{
            this.image = getURL("/Users/tony/Documents/McGill/W2025/Chess/res/pieces-basic-png/white-bishop.png");
        }
        System.out.println();
    }
    public Bishop (Bishop originalBishop, Board board){
        super(originalBishop,board);
    }
    public ArrayList<Move> getMoves() {
        return moves;
    }

    @Override
    public Bishop DeepCopy( Board newBoard ) {
        return new Bishop(this, newBoard);
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
        moves = new ArrayList<>();

        int currentRow = this.getRow();
        int currentCol = this.getCol();

        int[][] directions = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
        for (int[] direction : directions) {
            int newRow = currentRow + direction[0];
            int newCol = currentCol + direction[1];

            while (newRow >= 0 && newRow < 8 && newCol >= 0 && newCol < 8) {
                if (board.isEmpty(newRow, newCol)) {
                        moves.add(new Move(newRow, newCol));
                } else {
                    //Not empty different color -> can capture
                    if (!Objects.equals(board.getPiece(newRow, newCol).getColor(), this.getColor())) {
                            moves.add(new Move(newRow, newCol));
                    }else{
                        //not empty but same color
                        board.getProtectedSquares(isWhite).add(new Position(newRow, newCol));
                    }
                    break;
                }
                newRow += direction[0];
                newCol += direction[1];
            }
        }
    }
}
