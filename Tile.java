public class Tile {
    private final int cordX;
    private final int cordY;
    private boolean isEmpty;
    private Piece piece;

    public Tile(int cordX, int cordY, boolean isEmpty) {
        this.cordX = cordX;
        this.cordY = cordY;
        this.isEmpty = isEmpty;
    }
    public Piece getPiece() {
        return piece;
    }
}
