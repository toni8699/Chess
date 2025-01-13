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

    public Board() throws FileNotFoundException {
        board = new Piece[col][row];
        setBoard();

    }
    private void initBoard() throws FileNotFoundException {
    // Initialize pawns
    for (int i = 0; i < 8; i++) {
        board[1][i] = new Pawn(1, i, 0); // black pawns
        board[6][i] = new Pawn(6, i, 1); // white pawns
    }

    // Initialize rooks
    board[0][0] = new Rook(0, 0, 0); // black rook
    board[0][7] = new Rook(0, 7, 0); // black rook
    board[7][0] = new Rook(7, 0, 1); // white rook
    board[7][7] = new Rook(7, 7, 1); // white rook

    // Initialize bishops
    board[0][2] = new Bishop(0, 2, 0); // black bishop
    board[0][5] = new Bishop(0, 5, 0); // black bishop
    board[7][2] = new Bishop(7, 2, 1); // white bishop
    board[7][5] = new Bishop(7, 5, 1); // white bishop

    // Initialize knights
    board[0][1] = new Knight(0, 1, 0); // black knight
    board[0][6] = new Knight(0, 6, 0); // black knight
    board[7][1] = new Knight(7, 1, 1); // white knight
    board[7][6] = new Knight(7, 6, 1); // white knight
        System.out.println(board[7][6].getName());
        System.out.println("col: " +board[7][6].getX());

        // Initialize queens and kings
    board[0][3] = new Queen(0, 3, 0); // black queen
    board[0][4] = new King(0, 4, 0); // black king
    board[7][3] = new Queen(7, 3, 1); // white queen
    board[7][4] = new King(7, 4, 1); // white king
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
    public void setPiece(int col, int row, Piece piece){
        board[piece.getRow()][piece.getCol()] = null;
        board[row][col] = piece;
        piece.setCol(col);
        piece.setRow(row);
    }
    public void capture(Piece piece){
        capturedPieces.add(piece);
        System.out.println(piece.getName() + " captured" + piece.getRow() + " " + piece.getCol());
        board[piece.getRow()][piece.getCol()] = null;
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
}

