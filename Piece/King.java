package Piece;

import Main.Board;
import Main.Move;
import Main.Position;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Objects;

public class King extends Piece {
    private ArrayList<Piece> capturedPiece = new ArrayList<>();
    private ArrayList<Move> moves = new ArrayList<>();
    private final String name = "King";

    public King(int row, int col, Boolean isWhite, Board board) throws FileNotFoundException {
        super(row, col, isWhite,board);
        calculateMoves();
        if (!isWhite) {
            this.image = getURL("/Users/tony/Documents/McGill/W2025/Chess/res/pieces-basic-png/black-king.png");
        }else{
            this.image = getURL("/Users/tony/Documents/McGill/W2025/Chess/res/pieces-basic-png/white-king.png");
        }
    }

    // Copy constructor
    public King (King originalKing, Board board){
        super(originalKing,board);
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

    public ArrayList<Move> getMoves() {
        return moves;
    }

    @Override
    public King DeepCopy(Board newBoard) {
        return new King(this, newBoard);
    }

    public boolean canCastle(Boolean isKingSide) {
        if (!hasMoved() && !isIncheck()) {
            Piece rook = getRookForCastle(isKingSide);
            if (!(rook instanceof Rook)) {
                return false;
            }
            return !rook.hasMoved() && isPathClear(isKingSide) && !isPathUnderAttack(isKingSide);
        }
        return false;
    }

    public boolean isPathClear(boolean isKingSide) {
        if (isKingSide) {
            return (board.getPiece(this.getRow(), 6) == null && board.getPiece(this.getRow(), 5) == null);
        } else {
            return (board.getPiece(this.getRow(), 3) == null && board.getPiece(this.getRow(), 2) == null && board.getPiece(this.getRow(), 1) == null);
        }
    }

    /**
     * Check if the path from the king to the rook is under attack or not.
     * @param isKingSide true if the king is castling on the king side, false otherwise
     * @return true if the path is under attack, false otherwise
     */
    public boolean isPathUnderAttack(boolean isKingSide) {
        if (isKingSide) {
            for (Piece p : board.getActivePieces()) {
                if (!Objects.equals(p.getColor(), this.getColor()) && (p.canMove(this.getRow(), 6) || p.canMove(this.getRow(), 5))) {
                    return true;
                }
            }
        } else {
            for (Piece p : board.getActivePieces()) {
                if (!Objects.equals(p.getColor(), this.getColor()) && (p.canMove(this.getRow(), 3) || p.canMove(this.getRow(), 2))) {
                    return true;
                }
            }
        }
        return false;
    }

    public void addCastleMove(Boolean isKingSide) {
        if (isKingSide) {
            moves.add(new Move(this.getRow(), this.getCol() + 2));
        } else {
            moves.add(new Move(this.getRow(), this.getCol() - 2));
        }
    }

    public Piece getRookForCastle(Boolean isKingSide) {
        if (isKingSide) {
            if (this.isWhite()) {
                return board.getPiece(7, 7);
            } else {
                return board.getPiece(0, 7);
            }
        } else {
            if (this.isWhite()) {
                return board.getPiece(7, 0);
            } else {
                return board.getPiece(0, 0);
            }
        }
    }

    public boolean isIncheck() {
        for (Piece p : board.getActivePieces()) {
            if (!Objects.equals(p.getColor(), this.getColor()) && p.canMove(this.getRow(), this.getCol())) {
                return true;
            }
        }
        return false;
    }

    public boolean isSquaredUnderAttack(int row, int col) {
        for (Piece p : board.getActivePieces()) {
            if (!Objects.equals(p.getColor(), this.getColor())) {
                if (p instanceof Pawn) {
                    // Check the attack moves for the pawn
                    ArrayList<Move> attackMoves = ((Pawn) p).getAttackMoves();
                    for (Move move : attackMoves) {
                        if (move.getRow() == row && move.getCol() == col) {
                            return true;
                        }
                    }
                } else {
                    // Check regular moves for other pieces
                    if (p.canMove(row, col)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void calculateMoves() {
        moves = new ArrayList<>();
        if (canCastle(true)) {
            addCastleMove(true);
        }
        if (canCastle(false)) {
            addCastleMove(false);
        }

        int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {-1, -1}, {1, -1}, {-1, 1}};
        for (int[] direction : directions) {
            int newRow = this.getRow() + direction[0];
            int newCol = this.getCol() + direction[1];
            if (newRow >= 0 && newRow < 8 && newCol >= 0 && newCol < 8) {
                // Add the square to protected squares first
                board.getProtectedSquares(isWhite).add(new Position(newRow, newCol));

                if (board.isEmpty(newRow, newCol)){
                    if (!isSquaredUnderAttack(newRow, newCol)){
                        moves.add(new Move(newRow, newCol));
                    }
                }
                else{
                    if (!Objects.equals(board.getPiece(newRow, newCol).getColor(), this.getColor())) {
                        if (!board.getProtectedSquares(!isWhite).contains(new Position(newRow, newCol))) {
                            moves.add(new Move(newRow, newCol));
                        }
                    }
                }
            }
        }
    }
}