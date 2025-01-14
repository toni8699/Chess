package Piece;

import javafx.scene.image.Image;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class Piece {
    public Image image;
    private int x,y;
    private int col, row;
    private Boolean isWhite;
    private String name;


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
    public Boolean isWhite(){
        return isWhite;
    }

    public void setX(int x) {
        this.x = x;

    }
    public void setY(int y) {
        this.y = y;
    }
}
