package Main;

import java.awt.event.MouseAdapter;
import java.util.Arrays;

import Piece.*;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;

public class Mouse extends MouseAdapter {
    boolean clicked = false;
    Board board;
    public Mouse(Board board) {
        this.board = board;
    }
    public void mousePressed(MouseEvent e) {
        System.out.print("mouse pressed");
        int col = (int) (e.getX()/100);
        int row = (int) (e.getY()/100);
        Piece piece = board.getPiece(row, col);
        if (piece != null) {
            board.setSelectedPiece(piece);
        }
    }
//    public void mouseClicked(MouseEvent e) {
//        System.out.print("mouse clicked");
//        int col = (int) (e.getX() / 100);
//        int row = (int) (e.getY() / 100);
//        Piece selectedPiece = board.getSelectedPiece();
//
//        if (selectedPiece == null) {
//            // No piece is currently selected, select the piece at the clicked position
//            Piece piece = board.getPiece(row, col);
//            if (piece != null) {
//                board.setSelectedPiece(piece);
//                System.out.println("Selected piece: " + piece.getName());
//            }
//        } else {
//            // A piece is already selected, attempt to move it to the clicked position
//            Piece targetPiece = board.getPiece(row, col);
//            if (targetPiece == null || targetPiece.getColor() != selectedPiece.getColor()) {
//                // The target square is either empty or contains an opponent's piece
//                if (board.isValidMove(selectedPiece, row, col)) {
//                    int originalCol = selectedPiece.getCol();
//                    int originalRow = selectedPiece.getRow();
//                    System.out.println(selectedPiece.getName() + " moved from " + originalRow + "," + originalCol + " to " + row + "," + col);
//                    board.movePiece(col, row, selectedPiece);
//                    if (targetPiece != null) {
//                        System.out.println("Captured piece: " + targetPiece.getName());
//                    }
//                } else {
//                    System.out.println(selectedPiece.getName() + " cannot move to " + row + "," + col);
//                }
//                // Deselect the piece after attempting the move
//                board.setSelectedPiece(null);
//            } else {
//                // The target square contains a piece of the same color, re-select the piece
//                board.setSelectedPiece(targetPiece);
//                System.out.println("Re-selected piece: " + targetPiece.getName());
//            }
//        }
//    }

    public void mouseDragged(MouseEvent mouseEvent) {
        Piece p=  board.getSelectedPiece();
        p.setX((int) mouseEvent.getX() - 50);
        p.setY((int) mouseEvent.getY() - 50);

    }
/**
 * Handles the mouse released event.
 * Calculates the column and row based on the mouse position,
 * and attempts to place the currently selected piece on the board.
 * Prints the coordinates and the name of the selected piece if it exists.
 * Updates the board with the new position of the piece.
 *
 * @param e the MouseEvent triggering this method
 */
public void resetPiecePosition(Piece piece, int originalCol, int originalRow) {
    piece.setX(originalCol * 100);
    piece.setY(originalRow * 100);
}

public void mouseReleased(MouseEvent e) {
    System.out.println("mouse released");
    Piece p = board.getSelectedPiece();
    if (p != null) {
        int originalCol = p.getCol();
        int originalRow = p.getRow();
        int col = (int) (e.getX() / 100);
        int row = (int) (e.getY() / 100);
        if (board.isValidMove(p, row, col)) {
            System.out.println(p.getName() + " moved from " + originalRow + "," + originalCol + " to " + row + "," + col);
            board.movePiece(col, row, p);
        } else {
            // Reset the piece to its original position
            resetPiecePosition(p, originalCol, originalRow);
            System.out.println(p.getName() + " cannot move from " + originalRow + "," + originalCol + " to " + row + "," + col);
        }
        board.setSelectedPiece(null);
    }
}
}



