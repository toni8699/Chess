package legacy;


import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Objects;

public class Knight extends Piece {
    private final String name = "Knight";


    private ArrayList <Move> moves = new ArrayList<>();
    public Knight(int row, int col, Boolean isWhite, Board board) throws FileNotFoundException {
        super(row, col, isWhite,board);
        calculateMoves();
        System.out.println( name+ "moves : " + moves);

        if (!isWhite) {
            this.image = getURL("/Users/tony/Documents/McGill/W2025/Chess/res/pieces-basic-png/black-knight.png");
        }else{
            this.image = getURL("/Users/tony/Documents/McGill/W2025/Chess/res/pieces-basic-png/white-knight.png");
        }
    }
    public Knight (Knight originalKnight, Board board){
        super(originalKnight,board);
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
    public ArrayList<Move> getMoves() {
        return moves;
    }

    @Override
    public Knight DeepCopy(Board newBoard) {
        return new Knight(this,newBoard);
    }

    @Override
    public void calculateMoves() {
        moves = new ArrayList<>();
        int currentRow = this.getRow();
        int currentCol = this.getCol();
        int[][] directions = {{2, 1}, {2, -1}, {-2, 1}, {-2, -1}, {1, 2}, {1, -2}, {-1, 2}, {-1, -2}};
        for (int[] direction : directions) {
            int newRow = currentRow + direction[0];
            int newCol = currentCol + direction[1];
            if (newRow >= 0 && newRow < 8 && newCol >= 0 && newCol < 8) {
                if (board.isEmpty(newRow, newCol)) {
                    moves.add(new Move(newRow, newCol));
                } else {
                    if (!Objects.equals(board.getPiece(newRow, newCol).getColor(), this.getColor())) {
                        moves.add(new Move(newRow, newCol));
                    }
                }
            }

    }

}

}
