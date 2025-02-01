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
        board[1][i] = new Pawn(1, i, false); // black pawns
        board[6][i] = new Pawn(6, i, true); // white pawns
    }

    // Initialize rooks
    board[0][0] = new Rook(0, 0, false); // black rook
    board[0][7] = new Rook(0, 7, false); // black rook
    board[7][0] = new Rook(7, 0, true); // white rook
    board[7][7] = new Rook(7, 7, true); // white rook

    // Initialize bishops
    board[0][2] = new Bishop(0, 2, false); // black bishop
    board[0][5] = new Bishop(0, 5, false); // black bishop
    board[7][2] = new Bishop(7, 2, true); // white bishop
    board[7][5] = new Bishop(7, 5, true); // white bishop

    // Initialize knights
    board[0][1] = new Knight(0, 1, false); // black knight
    board[0][6] = new Knight(0, 6, false); // black knight
    board[7][1] = new Knight(7, 1, true); // white knight
    board[7][6] = new Knight(7, 6,true ); // white knight


        // Initialize queens and kings
    board[0][3] = new Queen(0, 3, false); // black queen
    board[0][4] = new King(0, 4, false); // black king
    board[7][3] = new Queen(7, 3, true); // white queen
    board[7][4] = new King(7, 4, true); // white king
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
    public boolean isValidMove(Piece piece, int row, int col){
        Piece pieceAtRowCol = board[row][col];
        if (isEmpty(row, col) || isCapturable(pieceAtRowCol)){
            if (pieceAtRowCol != null){
                capture(pieceAtRowCol);
            }
            return piece.canMove(row, col);
        }
        return false;
    }

    public void capture(Piece piece){
        capturedPieces.add(piece);
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
}

