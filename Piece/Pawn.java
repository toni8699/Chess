package Piece;

import Main.Board;
import Main.Move;
import Main.Position;

import java.io.FileNotFoundException;
import java.util.ArrayList;

public class Pawn extends Piece {
    private String name = "Pawn";
    private ArrayList<Move> moves = new ArrayList<>();
//    private ArrayList<Move> attackMoves = new ArrayList<>();
    public Pawn(int row, int col, Boolean isWhite , Board board) throws FileNotFoundException {
        super(row, col, isWhite,board);
        calculateMoves();
        if (!isWhite) {
            this.image = getURL("/Users/tony/Documents/McGill/W2025/Chess/res/pieces-basic-png/black-pawn.png");
        } else {
            this.image = getURL("/Users/tony/Documents/McGill/W2025/Chess/res/pieces-basic-png/white-pawn.png");
        }
    }
    public Pawn (Pawn originalPawn, Board board){
        super(originalPawn,board);
    }
    public ArrayList<Move> getMoves() {
        return moves;
    }

    @Override
    public Pawn DeepCopy(Board newBoard) {
        return new Pawn(this,newBoard);
    }

    @Override
    public String getName() {
        return name;
    }
    public void removeMove(Move move) {
        moves.remove(move);
    }
    public boolean canMove(int TargetRow, int TargetCol) {
        for (Move move : moves) {
            if (move.getRow() == TargetRow && move.getCol() == TargetCol) {
                return true;
        }
    }
        return false;
    }

    @Override
    public void calculateMoves() {
            moves = new ArrayList<>();
            int direction = isWhite() ? -1 : 1; // White pawns move up (-1), black pawns move down (+1)
            // Move forward one square
            if (board.isEmpty(getRow() + direction, getCol())) {
                moves.add(new Move(getRow() + direction, getCol()));
            }

            // Move forward two squares from starting position
            if (!hasMoved() && board.isEmpty(getRow() + direction, getCol()) && board.isEmpty(getRow() + 2 * direction, getCol())) {
                moves.add(new Move(getRow() + 2 * direction, getCol()));
            }
            //protected square diagonally
            board.getProtectedSquares(isWhite).add(new Position(getRow() + direction, getCol() - 1));
            board.getProtectedSquares(isWhite).add(new Position(getRow() + direction, getCol() + 1));
        // Capture diagonally left
            if (board.isValidCapture(getRow() + direction, getCol() - 1, this)) {
                moves.add(new Move(getRow() + direction, getCol() - 1));

            }
            // Capture diagonally right
            if (board.isValidCapture(getRow() + direction, getCol() + 1, this)) {
                moves.add(new Move(getRow() + direction, getCol() + 1));
            }
        }
    public ArrayList<Move> getAttackMoves() {
        ArrayList<Move> attackMoves = new ArrayList<>();
        int direction = isWhite() ? -1 : 1; // White pawns move up (-1), black pawns move down (+1)

        if (board.isValidCapture(getRow() + direction, getCol() - 1, this)) {
            attackMoves.add(new Move(getRow() + direction, getCol() - 1));
        }
        if (board.isValidCapture(getRow() + direction, getCol() + 1, this)) {
            attackMoves.add(new Move(getRow() + direction, getCol() + 1));
        }

        return attackMoves;
    }

}





