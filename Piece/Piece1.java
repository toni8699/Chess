//package Piece;
//
//import java.awt.*;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.InputStream;
//
//public  class Piece1 {
//    public Image image;
//    private int x,y;
//    private int col, row, prevcol, prevrow;
//    private int color;
//    private String name;
//
//    public Piece1(int row, int col, int color) {
//        this.row = row;
//        this.col = col;
//        this.color = color;
//        x = getX();
//        y = getY();
//        prevcol = col;
//        prevrow = row;
//    }
//
//    public int getY() {
//        return row * 100;
//    }
//
//    public int getX() {
//        return col *100;
//    }
//    public int getPrevcol(){
//        return prevcol;
//    }
//    public int getPrevrow(){
//        return prevrow;
//    }
//    public String getColor(){
//        if(color == 0){
//            return "Black";
//        }else{
//            return "White";
//        }
//    }
//    public Image getURL( String path) throws FileNotFoundException {
//        InputStream stream = new FileInputStream(path);
//        return new Image(stream);
//    }
//
//}