package Main;

import Piece.*;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class Board {
    final int col = 8;
    final int row = 8;
    private Piece[][] board;
    private ArrayList<Piece> activePieces = new ArrayList<>();
    private ArrayList<Piece> capturedPieces = new ArrayList<>();
    private Piece selectedPiece;
    private boolean whiteTurn = true;

    public Board() throws FileNotFoundException {
        board = new Piece[col][row];
        setBoard();

    }

    private void initBoard() throws FileNotFoundException {
    // Initialize pawns
    for (int i = 0; i < 8; i++) {
        board[1][i] = new Pawn(1, i, false,this); // black pawns
        board[6][i] = new Pawn(6, i, true,this); // white pawns
    }

    // Initialize rooks
    board[0][0] = new Rook(0, 0, false, this); // black rook
    board[0][7] = new Rook(0, 7, false,this); // black rook
    board[7][0] = new Rook(7, 0, true,this); // white rook
    board[7][7] = new Rook(7, 7, true,this); // white rook

    // Initialize bishops
    board[0][2] = new Bishop(0, 2, false,this); // black bishop
    board[0][5] = new Bishop(0, 5, false,this); // black bishop
    board[7][2] = new Bishop(7, 2, true,this); // white bishop
    board[7][5] = new Bishop(7, 5, true,this); // white bishop

    // Initialize knights
    board[0][1] = new Knight(0, 1, false,this); // black knight
    board[0][6] = new Knight(0, 6, false,this); // black knight
    board[7][1] = new Knight(7, 1, true,this); // white knight
    board[7][6] = new Knight(7, 6,true,this ); // white knight


        // Initialize queens and kings
    board[0][3] = new Queen(0, 3, false,this); // black queen
    board[0][4] = new King(0, 4, false,this); // black king
    board[7][3] = new Queen(7, 3, true,this); // white queen
    board[7][4] = new King(7, 4, true,this); // white king
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
    public void movePiece(int col, int row, Piece piece){
        if (isValidMove(piece, row, col)) {
            System.out.println(piece.getName() + " moved from " + piece.getRow() + "," + piece.getCol() + " to " + row + "," + col);
            board[piece.getRow()][piece.getCol()] = null;
            board[row][col] = piece;
            piece.setCol(col);
            piece.setRow(row);
            piece.setX(col * 100);
            piece.setY(row * 100);
            //recalculate moves
            piece.calculateMoves();

            System.out.println("moves: " + piece.getMoves());
            piece.setHasMoved(true);
            switchTurn();
        }else{
            System.out.println(piece.getName() + " cannot move from " + piece.getRow() + "," + piece.getCol() + " to " + row + "," + col);
        }

    }


    public boolean isEmpty(int row, int col){
        return board[row][col] == null;
    }
    public boolean isCapturable(Piece piece){

        return piece.isWhite() != selectedPiece.isWhite();
    }

    public boolean isValidCapture(int row, int col, Piece piece) {
        if (row < 0 || row >= 8 || col < 0 || col >= 8) {
            return false;
        }
        Piece targetPiece = board[row][col];
        return targetPiece != null && targetPiece.isWhite() != piece.isWhite();
    }

//    private boolean isValidPawnMove(Piece pawn, int targetRow, int targetCol) {
//        int direction = pawn.isWhite() ? -1 : 1;
//        int currentRow = pawn.getRow();
//        int currentCol = pawn.getCol();
//
//        // Move forward one square
//        if (targetRow == currentRow + direction && targetCol == currentCol && isEmpty(targetRow, targetCol)) {
//            return true;
//        }
//
//        // Move forward two squares from starting position
//        if (!pawn.hasMoved() && targetRow == currentRow + 2 * direction && targetCol == currentCol &&
//                isEmpty(currentRow + direction, currentCol) && isEmpty(targetRow, targetCol)) {
//            return true;
//        }
//
//        // Capture diagonally
//        if (targetRow == currentRow + direction && (targetCol == currentCol - 1 || targetCol == currentCol + 1) &&
//                isValidCapture(targetRow, targetCol, pawn)) {
//            pawn.addMoves(new Move(targetRow, targetCol));
//            return true;
//        }
//
//        return false;
//    }
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
    public boolean isValidMove(Piece piece, int targetRow, int targetCol) {
        Piece pieceAtRowCol = board[targetRow][targetCol];


        // Check if it's the correct player's turn
        if ((isWhiteTurn() && !piece.isWhite()) || (!isWhiteTurn() && piece.isWhite())) {
            System.out.println("Not your turn");
            return false;
        }

        // Check if the piece can legally move to the target location
        if (piece.canMove(targetRow, targetCol)) {
            if (piece instanceof Knight) {
                // Knights can jump over other pieces, so path clearance isn't necessary
                if (isEmpty(targetRow, targetCol) || isValidCapture(targetRow, targetCol, piece)) {
                    if (pieceAtRowCol != null) {
                        System.out.println(pieceAtRowCol.getName() + " captured");
                        capture(pieceAtRowCol);
                    }
                    return true;
                }
            } else {
                // For other pieces, ensure the path to the target location is clear
                if (isPathClear(piece.getRow(), piece.getCol(), targetRow, targetCol)) {
                    if (isEmpty(targetRow, targetCol) || isCapturable(pieceAtRowCol)) {
                        if (pieceAtRowCol != null) {
                            System.out.println(pieceAtRowCol.getName() + " captured");
                            capture(pieceAtRowCol);
                        }
                        return true;
                    }
                }
            }
        }

        return false;
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

    public Piece getSelectedPiece() {
        return selectedPiece;
    }

    public ArrayList<Piece> getCapturedPiece() {
        return capturedPieces;
    }
    public boolean isPathClear(int startRow, int startCol, int endRow, int endCol) {
        int rowStep = Integer.compare(endRow, startRow);
        int colStep = Integer.compare(endCol, startCol);

        int currentRow = startRow + rowStep;
        int currentCol = startCol + colStep;

        while (currentRow != endRow || currentCol != endCol) {
            if (board[currentRow][currentCol] != null) {
                return false;
            }
            currentRow += rowStep;
            currentCol += colStep;
        }

        return true;
    }
}

