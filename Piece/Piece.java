package Piece;

import Main.Board;
import Main.Move;
import javafx.scene.image.Image;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

public abstract class Piece {
    public Image image;
    protected int x,y;
    protected int col, row;
    protected final Boolean isWhite;
    protected String name;
    protected boolean hasMoved = false;
    protected ArrayList <Move> moves = new ArrayList<>();
    protected Board board;


    public Piece(int row, int col, Boolean isWhite, Board board) throws FileNotFoundException {
        this.row = row;
        this.col = col;
        this.isWhite = isWhite;
        x = col * 100;
        y = row * 100;
        this.board = board;
    }
    public Piece (Piece p, Board board){
        this.row = p.getRow();
        this.col = p.getCol();
        this.isWhite = p.isWhite();
        this.board = board;
        this.x = p.getX();
        this.y = p.getY();
        this.hasMoved = p.hasMoved();
    }

    public Image getURL(String path) {
        try {
            InputStream stream = new FileInputStream(path);
            return new Image(stream);
        } catch (FileNotFoundException e) {
            System.err.println("Error loading image: " + path);
            e.printStackTrace();
            // Create a simple colored rectangle as fallback
            int size = 100;
            javafx.scene.image.WritableImage image = new javafx.scene.image.WritableImage(size, size);
            javafx.scene.canvas.Canvas canvas = new javafx.scene.canvas.Canvas(size, size);
            javafx.scene.paint.Color color = isWhite ? javafx.scene.paint.Color.WHITE : javafx.scene.paint.Color.BLACK;
            canvas.getGraphicsContext2D().setFill(color);
            canvas.getGraphicsContext2D().fillRect(0, 0, size, size);
            return canvas.snapshot(null, null);
        }
    }
    public Image getImage(){
        return image;
    }

    public int getY() {
        return y;
    }

    public int getX() {
        return x;

    }
    public void setCol(int col) {
        this.col = col;
    }
    public void setRow(int row) {
        this.row = row;
    }
    public int getCol(){
        return col;
    }
    public int getRow(){
        return row;
    }
    public String getName(){
        return name;
    }
    public String getColor(){
        if (isWhite){
            return "White";
        }else{
            return "Black";
        }
    }
    public Boolean isWhite(){
        return isWhite;
    }

    public void setX(int x) {
        this.x = x;

    }
    public void setY(int y) {
        this.y = y;
    }
    public abstract boolean canMove(int row, int col);
    public abstract void calculateMoves();

    public ArrayList<Move> getMoves() {
        for (Move move : moves) {
            System.out.println( this.getName()  +"moves : " + move.getRow() + " " + move.getCol());
        }
        return moves;
    }

    public boolean hasMoved() {
        return hasMoved;
    }
    public void setHasMoved(boolean hasMoved) {
        this.hasMoved = hasMoved;
    }
    public abstract Piece DeepCopy( Board newBoard);
}

