package Main;

import java.awt.event.MouseAdapter;
import java.util.Arrays;

import Piece.*;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;

public class Mouse extends MouseAdapter {
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
    public void mouseReleased(MouseEvent e) {
        System.out.println("mouse released");
        Piece p = board.getSelectedPiece();
        if (board.getSelectedPiece() != null) {
            int col = (int) (e.getX()/100);
            int row = (int) (e.getY()/100);
            p.setX(col*100);
            p.setY(row*100);
            board.movePiece(col, row, p);
            System.out.println("placed " + board.getSelectedPiece().getName() + " at " + col + "," + row);
        }
        board.setSelectedPiece(null);
    }}




//    @Override
//    public void mousePressed(MouseEvent e) {
//        System.out.print("mouse pressed");
//        int col = e.getX()/100;
//        int row = e.getY()/100;
//        Piece piece = board.getPiece(row, col);
//        if (piece != null) {
//            board.setSelectedPiece(piece);
//        }
//    }
//    @Override
//    public void mouseReleased(MouseEvent e) {
//        if (board.getSelectedPiece() != null) {
//            board.setPiece(board.getSelectedPiece().getX()/100, board.getSelectedPiece().getY()/100, board.getSelectedPiece());
//        }
//        board.setSelectedPiece(null);
//    }
//
//
//    @Override
//    public void mouseDragged(MouseEvent e) {
//        if (board.getSelectedPiece() != null) {
//            board.getSelectedPiece().setX(e.getX()-50);
//            board.getSelectedPiece().setY(e.getY()-50);
//        }
//    }
//    @Override
//    public void mouseMoved(MouseEvent e) {
//
//    }
//
//    @Override
//    public void handle(T t) {
//
//    }
//}

