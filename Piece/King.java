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
    public boolean canCastle( Boolean isKingSide){
        System.out.println("can castle?");
        if (hasMoved()){
            System.out.println("already moved");
            return false;
        }
        if (isKingSide){

            return board.getPiece(this.getRow(), this.getCol() + 1) == null && board.getPiece(this.getRow(), this.getCol() + 2) == null && !getRookForCastle(true).hasMoved();
        }else{
            return !getRookForCastle(false).hasMoved() && board.getPiece(this.getRow(), this.getCol() - 1) == null && board.getPiece(this.getRow(), this.getCol() - 2) == null && board.getPiece(this.getRow(), this.getCol() - 3) == null;
        }
    }
    public void addCastleMove( Boolean isKingSide){
        if (isKingSide){
            moves.add(new Move(this.getRow(), this.getCol() + 2));
        }else{
            moves.add(new Move(this.getRow(), this.getCol() - 2));
        }

    }
    public Rook getRookForCastle(Boolean isKingSide){
        if (isKingSide){
            return (Rook) board.getPiece(this.getRow(), 7);
        }else{
            return (Rook) board.getPiece(this.getRow(), 0);
        }
    }





    @Override
    public void calculateMoves() {
        moves = new ArrayList<>();
        if (canCastle(true)){
            System.out.println("can castle king");
            addCastleMove(true);
        }
        if (canCastle(false)){
            System.out.println("can castle queen");
            addCastleMove(false);
        }

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
