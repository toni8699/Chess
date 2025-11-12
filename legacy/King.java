package legacy;


import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Objects;

public class King extends Piece {
    private ArrayList<Piece> capturedPiece = new ArrayList<>();
    private ArrayList <Move> moves = new ArrayList<>();
    private final String name = "King";
//    public Board board;



    public King(int row, int col, Boolean isWhite, Board board) throws FileNotFoundException {
        super(row, col, isWhite,board);
//        this.board = board;
        calculateMoves();
        if (!isWhite) {
            this.image = getURL("/Users/tony/Documents/McGill/W2025/Chess/res/pieces-basic-png/black-king.png");
        }else{
            this.image = getURL("/Users/tony/Documents/McGill/W2025/Chess/res/pieces-basic-png/white-king.png");
        }
    }
    //Copy constructor
    public King (King originalKing, Board board){
        super(originalKing,board);
//        this.board = board;
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
//        System.out.println("can't move");
        return false;

    }
    public ArrayList<Move> getMoves() {
        return moves;
    }

    @Override
    public King DeepCopy( Board newBoard) {
        return new King(this,newBoard);
    }

    public boolean canCastle( Boolean isKingSide) {
        if ( isIncheck()||hasMoved() || getRookForCastle(isKingSide) == null) {
//            System.out.println("can't castle Rook does not exist or king is in check");
            return false;
        }
        if (isPathUnderAttack(isKingSide)) {
//            System.out.println("Path under attack");
            return false;
        }
        if (!isPathClear(isKingSide)) {
//            System.out.println("Path not clear");
            return false;
        }
        System.out.println("can castle" + isKingSide);
        return true;
    }

    public boolean isPathClear(boolean isKingSide) {
        if (isKingSide) {
            return (board.getPiece(this.getRow(), 6) == null && board.getPiece(this.getRow(),5) == null );
        }else{
            return (board.getPiece(this.getRow(), 3) == null && board.getPiece(this.getRow(),2) == null && board.getPiece(this.getRow(),1) == null);
        }
    }

    /**
     * Check if the path from the king to the rook is under attack or not.
     * @param isKingSide true if the king is castling on the king side, false otherwise
     * @return true if the path is under attack, false otherwise
     */
    public boolean isPathUnderAttack(boolean isKingSide) {
//        System.out.println("king Checking " +this.getColor());
        if (isKingSide) {
            for (Piece p : board.getActivePieces()) {
                if (!Objects.equals(p.getColor(), this.getColor()) &&( p.canMove(this.getRow(), 6) || p.canMove(this.getRow(), 5))) {
//                    System.out.println(this.getColor() +" king Path under attack by " + p.getName());
                    return true;
                }
            }
        }else{
            for (Piece p : board.getActivePieces()) {
                if (!Objects.equals(p.getColor(), this.getColor()) &&( p.canMove(this.getRow(), 3) || p.canMove(this.getRow(), 2))) {
//                    System.out.println("Path under attack by " + p.getName());
                    return true;
                }
            }
        }
        return false;
    }


    public void addCastleMove( Boolean isKingSide){
        if (isKingSide){
            moves.add(new Move(this.getRow(), this.getCol() + 2));
        }else{
            moves.add(new Move(this.getRow(), this.getCol() - 2));
        }

    }
    public Rook getRookForCastle(Boolean isKingSide) {
        int col = isKingSide ? 7 : 0;
        Piece piece = board.getPiece(this.getRow(), col);
        return (piece instanceof Rook) ? (Rook) piece : null;
    }
    public boolean isIncheck(){
        for (Piece p : board.getActivePieces()){
            if (!Objects.equals(p.getColor(), this.getColor()) && p.canMove(this.getRow(), this.getCol())){
                return true;
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
