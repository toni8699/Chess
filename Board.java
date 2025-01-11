public class Board {
    private final int col = 8;
    private final int row = 8;
    private Tile[][] board = new Tile[col][row];

    public Board()
    {


    }
    public void printBoard()
    {
        for (int i = 0; i < col; i++) {
            for (int j = 0; j < row; j++) {
                System.out.print(board[i][j] + " ");
            }
        }
    }
}
