package Main;

import java.awt.event.MouseAdapter;
import java.util.Arrays;

import Piece.*;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;

public class Mouse extends MouseAdapter {
    boolean clicked = false;
    GamePanel panel;
    Board board;
    public Mouse(Board board, GamePanel panel) {
        this.board = board;
        this.panel = panel;
    }
    public void mousePressed(MouseEvent e) {
        System.out.print("mouse pressed");
        int col = (int) (e.getX()/100);
        int row = (int) (e.getY()/100);
        Piece piece = board.getPiece(row, col);
        if (piece != null) {
            piece.calculateMoves();
            board.setSelectedPiece(piece);
            panel.drawHighlightedMoves(piece);
            panel.update();

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
            board.movePiece(col, row, p);
        } else {
            // Reset the piece to its original position
            resetPiecePosition(p, originalCol, originalRow);
        }
        board.setSelectedPiece(null);
    }
}
}



