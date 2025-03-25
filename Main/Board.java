package Main;

import Piece.*;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static java.lang.System.exit;

public class Board {
    final int col = 8;
    final int row = 8;
    private Piece[][] board;
    private ArrayList<Piece> activePieces = new ArrayList<>();
    private ArrayList<Piece> capturedPieces = new ArrayList<>();
    private Piece selectedPiece;
    private Piece lastMovedPiece;
    private boolean whiteTurn = true;
    private King whiteKing;
    private  King blackKing;
    private Set<Position> whiteProtected = new HashSet<>();
    private Set<Position> blackProtected = new HashSet<>();

    public Board() throws FileNotFoundException {
        board = new Piece[col][row];
        activePieces = new ArrayList<>();
        capturedPieces = new ArrayList<>();
        setBoard();
        whiteKing = getKing(true);
        blackKing = getKing(false);
        printBoard();
    }
    public Board(Board originalBoard) throws FileNotFoundException {
         board = new Piece[col][row];
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (originalBoard.board[i][j] != null) {
                    board[i][j] = originalBoard.board[i][j].DeepCopy(this);
                    activePieces.add(board[i][j]);
                }
            }
        }
        whiteKing = getKing(true);
        blackKing = getKing(false);
        lastMovedPiece = originalBoard.lastMovedPiece;
        selectedPiece = originalBoard.selectedPiece;
        whiteTurn = originalBoard.whiteTurn;
    }

    public boolean isKingInCheck( Boolean isWhiteTurn){
        if (isWhiteTurn()){
//            System.out.println("White is in check");
            System.out.println(this.whiteKing.isIncheck());

            return this.whiteKing.isIncheck();

        }else{
//            System.out.println("Black is in check");
            System.out.println(this.blackKing.isIncheck());

            return this.blackKing.isIncheck();
        }
    }

    public Rook getKingRook(King king){
        if (king.getRow() == 0){
            return (Rook) board[0][7];
        }else{
            return (Rook) board[7][7];
        }
    }

    public Rook getQueenRook(King king){
        if (king.getRow() == 0){
            return (Rook) board[0][0];
        }else{
            return (Rook) board[7][0];
        }
    }
    private King getKing (Boolean isWhite){
        for (Piece p : activePieces) {
            if (p instanceof King && p.isWhite() == isWhite) {
                return (King) p;
            }
        }
        return null;
    }
    private void Castle(King king,Boolean isKingSide) {
        if (isKingSide){
            Rook rook = getKingRook(king);
            rook.setHasMoved(true);
            king.setHasMoved(true);
            board[king.getRow()][king.getCol() + 1] = rook;
            board[king.getRow()][king.getCol()+2]=king;
            board[king.getRow()][king.getCol()] = null;
            board[rook.getRow()][rook.getCol()] = null;
            rook.setCol(king.getCol() + 1);
            king.setCol(king.getCol() + 2);
            king.setX(king.getCol() * 100);
            rook.setX(rook.getCol() * 100);

        }else{
            Rook rook = getQueenRook(king);
            rook.setHasMoved(true);
            king.setHasMoved(true);
            board[king.getRow()][king.getCol() - 1] = rook;
            board[king.getRow()][king.getCol()-2]=king;
            board[king.getRow()][king.getCol()] = null;
            board[rook.getRow()][rook.getCol()] = null;
            rook.setCol(king.getCol() - 1);
            king.setCol(king.getCol() - 2);
            king.setX(king.getCol() * 100);
            rook.setX(rook.getCol() * 100);
        }


    }

    private void initBoard() throws FileNotFoundException {
//     Initialize pawns
//    for (int i = 0; i < 8; i++) {
//        board[1][i] = new Pawn(1, i, false,this); // black pawns
//        board[6][i] = new Pawn(6, i, true,this); // white pawns
//    }

////     Initialize rooks
//    board[0][0] = new Rook(0, 0, false, this); // black rook
//    board[0][7] = new Rook(0, 7, false,this); // black rook
//    board[7][0] = new Rook(7, 0, true,this); // white rook
//    board[7][7] = new Rook(7, 7, true,this); // white rook

////     Initialize bishops
//    board[0][2] = new Bishop(0, 2, false,this); // black bishop
//    board[0][5] = new Bishop(0, 5, false,this); // black bishop
//    board[7][2] = new Bishop(7, 2, true,this); // white bishop
//    board[7][5] = new Bishop(7, 5, true,this); // white bishop

////     Initialize knights
//    board[0][1] = new Knight(0, 1, false,this); // black knight
//    board[0][6] = new Knight(0, 6, false,this); // black knight
//    board[7][1] = new Knight(7, 1, true,this); // white knight
//    board[7][6] = new Knight(7, 6,true,this ); // white knight


        // Initialize queens and kings
    board[0][3] = new Queen(0, 3, false,this); // black queen
        King blackKing = new King(0, 4, false,this);
    board[0][4] = blackKing; // black king
//    board[7][3] = new Queen(7, 3, true,this); // white queen
        King whiteKing = new King(7, 4, true,this);
    board[7][4] = whiteKing; // white king
}

    /**
     * Initialize the board with a valid starting configuration of pieces.
     */
    private void setBoard() throws FileNotFoundException {
        initBoard();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (board[i][j] != null) {
                    activePieces.add(board[i][j]);
                }
            }
        }
    }
    public void printBoard(){
        System.out.println("Printing board");
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (board[i][j] == null) {
                    System.out.print("_ ");
                }else{
                    System.out.print(board[i][j].getName().charAt(0)     + " ");

                }
            }
            System.out.println();
        }
    }
    public Piece getPiece(int row, int col){
        return board[row][col];

    }
    /**
     * Moves the specified piece to a new position on the board.
     *
     * @param col the target column index to move the piece to
     * @param row the target row index to move the piece to
     * @param piece the Piece object to be moved
     */
    public boolean movePiece(int col, int row, Piece piece) throws FileNotFoundException {

        if (isValidMove(piece, row, col) ) {
            switchTurn();
            piece.setHasMoved(true);

            if (piece instanceof King) {
                if (col == piece.getCol() + 2) {
                    Castle((King) piece, true);
                } else if (col == piece.getCol() - 2) {
                    Castle((King) piece, false);
                }
            }
            board[piece.getRow()][piece.getCol()] = null;
            Piece pAtTarget = board[row][col];
            if (pAtTarget != null) {
                capture(pAtTarget);
            }
            board[row][col] = piece;
            piece.setCol(col);
            piece.setRow(row);
            piece.setX(col * 100);
            piece.setY(row * 100);
            //recalculate moves
            updateMoves();
            if (whiteTurn){
                StaleMateOrCheckMate(whiteKing);
            }else{
                StaleMateOrCheckMate(blackKing);
            }
            return true;
        }else{
            System.out.println("Invalid move for " + piece.getName() + " from " + piece.getRow() + "," + piece.getCol() + " to " + row + "," + col);
        }

        return false;

    }
    // Add this method to your Board class
    public boolean isDraw(boolean isWhiteTurn) throws FileNotFoundException {
        King king = isWhiteTurn ? whiteKing : blackKing;

        // Check if any piece has a legal move
        for (Piece piece : activePieces) {
            if (piece.isWhite() == isWhiteTurn) {
                piece.calculateMoves();  // Ensure the moves are updated
                for (Move move : piece.getMoves()) {
                    // Check if the move leaves the king in check
                    if (!moveLeavesKingInCheck(piece, move.getRow(), move.getCol())) {
                        return false;  // Found a legal move
                    }
                }
            }
        }

        // If no legal moves are found, it's a draw
        return true;
    }

    private void StaleMateOrCheckMate(King king) throws FileNotFoundException {
        king.calculateMoves();
       if (king.getMoves().isEmpty()) {
           if (king.isIncheck()){
               System.out.println("Checkmate");
               exit(0);
           }else{
               if (isDraw( king.isWhite())){
                   System.out.println("Draw");
                   exit(0);
               };

           }
       }
    }
    public boolean isEmpty(int row, int col){
        return board[row][col] == null;
    }


    public boolean isValidCapture(int row, int col, Piece piece) {
        if (row < 0 || row >= 8 || col < 0 || col >= 8) {
            return false;
        }
        Piece targetPiece = board[row][col];
        return targetPiece != null && targetPiece.isWhite() != piece.isWhite();
    }
    public void removePiece ( Piece piece){
        board [piece.getRow()][piece.getCol()] = null;
        for (int i = 0; i < activePieces.size(); i++) {
            if (activePieces.get(i) == piece) {
                activePieces.remove(i);
                break;
            }
        }

    }
    /**
     * Determines if a piece can be moved to a specific target location.
     * Checks if the target location is empty or occupied by a piece of
     * the opposite color. If the target location is occupied by a piece
     * of the opposite color, the piece is captured and removed from the board.
     *
     * @param piece the Piece object to be moved
     * @param targetRow the target row index to move the piece to
     * @param targetCol the target column index to move the piece to
     * @return true if the piece can be moved to the target location, false otherwise
     */
    public boolean isValidMove(Piece piece, int targetRow, int targetCol) throws FileNotFoundException {
        Piece pieceAtRowCol = board[targetRow][targetCol];
//         Check if it's the correct player's turn
        if ((isWhiteTurn() && !piece.isWhite()) || (!isWhiteTurn() && piece.isWhite())) {
            System.out.println("Not your turn");
            return false;
        }
        if (moveLeavesKingInCheck(piece, targetRow, targetCol)){
            System.out.println(" move leaves King in check");
            return false;
        }
        if (piece.canMove(targetRow, targetCol)) {
            return true;
        }
//         Check if the piece can legally move to the target loca
        System.out.println("not a valid move");
        return false;
    }

    public boolean moveLeavesKingInCheck(Piece p , int targetRow, int targetCol) throws FileNotFoundException {
        Board tempBoard = new Board(this);
        Piece tempPiece = tempBoard.getPiece(p.getRow(), p.getCol());
        Piece targetPiece = tempBoard.getPiece(targetRow, targetCol);
        King king ;
        if (p.isWhite()){
            king = tempBoard.whiteKing;
        }else{
            king = tempBoard.blackKing;
        }
        if (targetPiece!=null){
            tempBoard.removePiece(targetPiece);

        }
        tempBoard.board[targetRow][targetCol] = tempPiece;
        tempBoard.board[p.getRow()][p.getCol()] = null;
        tempPiece.setRow(targetRow);
        tempPiece.setCol(targetCol);
//        System.out.println("printing tmp board");
//        tempBoard.printBoard();
        for (Piece piece : tempBoard.activePieces) {
            if (!Objects.equals(piece.getColor(), p.getColor()))
                piece.calculateMoves();
        }
        if (king.isIncheck()){
            System.out.println(king.getColor() + " in check can't move");
            return true;
        }else{
            System.out.println(king.getColor() + " not in check");
        }
        return false;
    }

    public void updateMoves() {
        whiteProtected.clear();
        blackProtected.clear();
        for (Piece p : activePieces) {
            p.calculateMoves();
        }
    }
    public Set<Position> getProtectedSquares(boolean isWhite) {
        if (isWhite) {
            return whiteProtected;
        } else {
            return blackProtected;
        }
    }

    




    public boolean isProtectedSquare(int row, int col, boolean isWhite) {
        return  getProtectedSquares(isWhite).contains(new Position(row, col));
    }

    public void capture(Piece piece){
        capturedPieces.add(piece);
        activePieces.remove(piece);
        System.out.println(piece.getName() + " captured" + piece.getRow() + " " + piece.getCol());
        board[piece.getRow()][piece.getCol()] = null;
    }
    public void switchTurn(){
        whiteTurn = !whiteTurn;
    }
    public boolean isWhiteTurn(){
        return whiteTurn;
    }
    public Piece[][] getPieces(){
        return board;
    }

    public ArrayList<Piece> getActivePieces() {
        return activePieces;
    }

    public void setSelectedPiece(Piece piece) {
            selectedPiece = piece;
    }
    public void setLastMovedPiece(Piece piece) {
        lastMovedPiece = piece;
    }
    public Piece getLastMovedPiece(){
        return lastMovedPiece;
    }

    public Piece getSelectedPiece() {
        return selectedPiece;
    }
    public ArrayList<Piece> getCapturedPiece() {
        return capturedPieces;
    }
    // Inside your Board class
    public void resetBoard() throws FileNotFoundException {
        // Clear the current board state
        for (int i = 0; i < col; i++) {
            for (int j = 0; j < row; j++) {
                board[i][j] = null;
            }
        }
        activePieces.clear();
        capturedPieces.clear();
        selectedPiece = null;
        lastMovedPiece = null;
        whiteTurn = true;

        // Reinitialize the board
        setBoard();
    }
}
