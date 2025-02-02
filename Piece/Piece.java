package Piece;

import Main.Move;
import javafx.scene.image.Image;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

public class Piece {
    public Image image;
    private int x,y;
    private int col, row;
    private final Boolean isWhite;
    private String name;
    private boolean hasMoved = false;
    private ArrayList <Move> moves = new ArrayList<>();


    public Piece(int row, int col, Boolean isWhite) {
        this.row = row;
        this.col = col;
        this.isWhite = isWhite;
        x = col * 100;
        y = row * 100;
    }

    public Image getURL( String path) throws FileNotFoundException {
        InputStream stream = new FileInputStream(path);
        return new Image(stream);
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
    public boolean canMove(int row, int col){
        return true;
    }
    public void calculateMoves(){
        return ;
    }

    public ArrayList<Move> getMoves() {
        return moves;
    }

    public void setMoves(ArrayList<Move> moves) {
        this.moves = moves;
    }
    public boolean hasMoved() {
        return hasMoved;
    }
    public void setHasMoved(boolean hasMoved) {
        this.hasMoved = hasMoved;
    }
}

