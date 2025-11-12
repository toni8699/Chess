package legacy;


import java.io.FileNotFoundException;
import java.util.ArrayList;

public class Queen extends Piece {
    private final String name = "Queen";

    private ArrayList <Move> moves = new ArrayList<>();
    public Queen(int row, int col, Boolean isWhite, Board board) throws FileNotFoundException {
        super(row, col, isWhite,board);
        calculateMoves();
//
        if (!isWhite) {
            this.image = getURL("/Users/tony/Documents/McGill/W2025/Chess/res/pieces-basic-png/black-queen.png");
        }else{
            this.image = getURL("/Users/tony/Documents/McGill/W2025/Chess/res/pieces-basic-png/white-queen.png");
        }
    }
    public Queen(Queen originalQueen, Board board){
       super (originalQueen, board);
    }
    public ArrayList<Move> getMoves() {
        return moves;
    }

    @Override
    public Queen DeepCopy(Board newBoard) {
        return new Queen (this,newBoard);
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
        moves = new ArrayList<>();
        int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {-1, -1}, {1, -1}, {-1, 1}};
        for (int[] direction : directions) {
            int newRow = this.getRow() + direction[0];
            int newCol = this.getCol() + direction[1];

            while (newRow >= 0 && newRow < 8 && newCol >= 0 && newCol < 8) {
                if (board.isEmpty(newRow, newCol)) {
                    moves.add(new Move(newRow, newCol));
                } else {
                    if (board.getPiece(newRow, newCol).getColor() != this.getColor()) {
                        moves.add(new Move(newRow, newCol));
                    }
                    break;
                }
                newRow += direction[0];
                newCol += direction[1];
            }
        }


    }
}
