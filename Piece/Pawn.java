package Piece;

import Main.Board;
import Main.Move;
import Main.Position;

import java.io.FileNotFoundException;
import java.util.ArrayList;

public class Pawn extends Piece {
    private String name = "Pawn";
    private ArrayList<Move> moves = new ArrayList<>();

    public Pawn(int row, int col, Boolean isWhite , Board board) throws FileNotFoundException {
        super(row, col, isWhite,board);
        calculateMoves();
        System.out.println("moves: " + moves);
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
                System.out.println("can move");
                return true;
        }
    }
        return false;
    }

    @Override
    public void calculateMoves() {
        moves = new ArrayList<>();
        int direction = isWhite() ? -1 : 1; // White pawns move up (-1), black pawns move down (+1)
        int newRow = getRow() + direction;
        
        // Check if newRow is within bounds before proceeding
        if (newRow >= 0 && newRow < 8) {
            // Move forward one square
            if (board.isEmpty(newRow, getCol())) {
                moves.add(new Move(newRow, getCol()));
            }

            // Move forward two squares from starting position
            if (!hasMoved() && board.isEmpty(newRow, getCol())) {
                int twoSquaresRow = getRow() + 2 * direction;
                if (twoSquaresRow >= 0 && twoSquaresRow < 8 && board.isEmpty(twoSquaresRow, getCol())) {
                    moves.add(new Move(twoSquaresRow, getCol()));
                }
            }

            // Capture diagonally left
            if (getCol() > 0 && board.isValidCapture(newRow, getCol() - 1, this)) {
                moves.add(new Move(newRow, getCol() - 1));
            }
            // Capture diagonally right
            if (getCol() < 7 && board.isValidCapture(newRow, getCol() + 1, this)) {
                moves.add(new Move(newRow, getCol() + 1));
            }
        }

        Position enPassantTarget = board.getEnPassantTarget();
        if (enPassantTarget != null) {
            int targetRow = getRow() + direction;
            if (targetRow == enPassantTarget.getRow() && Math.abs(enPassantTarget.getCol() - getCol()) == 1) {
                moves.add(new Move(enPassantTarget.getRow(), enPassantTarget.getCol()));
            }
        }

    }
}
