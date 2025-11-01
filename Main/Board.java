package Main;

import Piece.*;
import java.io.FileNotFoundException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;

public class Board {
    final int col = 8;
    final int row = 8;
    private Piece[][] board;
    private ArrayList<Piece> activePieces = new ArrayList<>();
    private ArrayList<Piece> capturedPieces = new ArrayList<>();
    private ArrayList<String> moveHistory = new ArrayList<>();
    private Piece selectedPiece;
    private Piece lastMovedPiece;
    private boolean whiteTurn = true;
    private King whiteKing;
    private King blackKing;
    private GameState gameState = GameState.PLAYING;
    private Position enPassantTarget;
    private final Deque<Board> history = new ArrayDeque<>();

    public Board() throws FileNotFoundException {
        board = new Piece[col][row];
        activePieces = new ArrayList<>();
        capturedPieces = new ArrayList<>();
        moveHistory = new ArrayList<>();
        setBoard();
        whiteKing = getKing(true);
        blackKing = getKing(false);
        enPassantTarget = null;
        updateGameState(); // Initialize game state
        printBoard();
    }
    public Board(Board originalBoard) throws FileNotFoundException {
        board = new Piece[col][row];
        activePieces = new ArrayList<>();
        capturedPieces = new ArrayList<>();
        moveHistory = new ArrayList<>(originalBoard.moveHistory);
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (originalBoard.board[i][j] != null) {
                    board[i][j] = originalBoard.board[i][j].DeepCopy(this);
                    activePieces.add(board[i][j]);
                }
            }
        }
        whiteKing = getKing(true);
        blackKing = getKing(false);
        lastMovedPiece = originalBoard.lastMovedPiece;
        selectedPiece = originalBoard.selectedPiece;
        whiteTurn = originalBoard.whiteTurn;
        if (originalBoard.enPassantTarget != null) {
            enPassantTarget = new Position(originalBoard.enPassantTarget.getRow(), originalBoard.enPassantTarget.getCol());
        }
        gameState = GameState.PLAYING; // Will be recalculated if needed
    }
//    public Board DeepCopy(){
//        return new Board(this);
//    }
    public boolean isKingInCheck( Boolean isWhiteTurn){
        if (isWhiteTurn()){
            System.out.println("White is in check");
            System.out.println(this.whiteKing.isIncheck());

            return this.whiteKing.isIncheck();

        }else{
            System.out.println("Black is in check");
            System.out.println(this.blackKing.isIncheck());

            return this.blackKing.isIncheck();
        }
    }

    public Rook getKingRook(King king){
        if (king.getRow() == 0){
            return (Rook) board[0][7];
        }else{
            return (Rook) board[7][7];
        }
    }

    public Rook getQueenRook(King king){
        if (king.getRow() == 0){
            return (Rook) board[0][0];
        }else{
            return (Rook) board[7][0];
        }
    }
    private King getKing (Boolean isWhite){
        for (Piece p : activePieces) {
            if (p instanceof King && p.isWhite() == isWhite) {
                return (King) p;
            }
        }
        return null;
    }
    private void Castle(King king,Boolean isKingSide) {
        if (isKingSide){
            Rook rook = getKingRook(king);
            rook.setHasMoved(true);
            king.setHasMoved(true);
            board[king.getRow()][king.getCol() + 1] = rook;
            board[king.getRow()][king.getCol()+2]=king;
            board[king.getRow()][king.getCol()] = null;
            board[rook.getRow()][rook.getCol()] = null;
            rook.setCol(king.getCol() + 1);
            king.setCol(king.getCol() + 2);
            king.setX(king.getCol() * 100);
            rook.setX(rook.getCol() * 100);

        }else{
            Rook rook = getQueenRook(king);
            rook.setHasMoved(true);
            king.setHasMoved(true);
            board[king.getRow()][king.getCol() - 1] = rook;
            board[king.getRow()][king.getCol()-2]=king;
            board[king.getRow()][king.getCol()] = null;
            board[rook.getRow()][rook.getCol()] = null;
            rook.setCol(king.getCol() - 1);
            king.setCol(king.getCol() - 2);
            king.setX(king.getCol() * 100);
            rook.setX(rook.getCol() * 100);
        }


    }

    private void initBoard() throws FileNotFoundException {
    // Initialize pawns
    for (int i = 0; i < 8; i++) {
        board[1][i] = new Pawn(1, i, false,this); // black pawns
        board[6][i] = new Pawn(6, i, true,this); // white pawns
    }

//     Initialize rooks
    board[0][0] = new Rook(0, 0, false, this); // black rook
    board[0][7] = new Rook(0, 7, false,this); // black rook
    board[7][0] = new Rook(7, 0, true,this); // white rook
    board[7][7] = new Rook(7, 7, true,this); // white rook

    // Initialize bishops
    board[0][2] = new Bishop(0, 2, false,this); // black bishop
    board[0][5] = new Bishop(0, 5, false,this); // black bishop
    board[7][2] = new Bishop(7, 2, true,this); // white bishop
    board[7][5] = new Bishop(7, 5, true,this); // white bishop

//     Initialize knights
    board[0][1] = new Knight(0, 1, false,this); // black knight
    board[0][6] = new Knight(0, 6, false,this); // black knight
    board[7][1] = new Knight(7, 1, true,this); // white knight
    board[7][6] = new Knight(7, 6,true,this ); // white knight


        // Initialize queens and kings
    board[0][3] = new Queen(0, 3, false,this); // black queen
        King blackKing = new King(0, 4, false,this);
    board[0][4] = blackKing; // black king
    board[7][3] = new Queen(7, 3, true,this); // white queen
        King whiteKing = new King(7, 4, true,this);
    board[7][4] = whiteKing; // white king
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
        System.out.println("Printing board");
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
    /**
     * Moves the specified piece to a new position on the board.
     *
     * @param col the target column index to move the piece to
     * @param row the target row index to move the piece to
     * @param piece the Piece object to be moved
     */
    public MoveResult movePiece(int col, int row, Piece piece) throws FileNotFoundException {
        return movePiece(col, row, piece, null);
    }

    public MoveResult movePiece(int col, int row, Piece piece, PromotionType promotionType) throws FileNotFoundException {
        if (piece == null) {
            return MoveResult.illegal("No piece selected");
        }

        if (gameState != GameState.PLAYING && gameState != GameState.CHECK) {
            return MoveResult.illegal("Game already finished");
        }

        MoveResult validation = validateMove(piece, row, col);
        if (!validation.isDone()) {
            return validation;
        }

        int fromRow = piece.getRow();
        int fromCol = piece.getCol();
        boolean promotionSquare = piece instanceof Pawn && (row == 0 || row == 7);
        if (!promotionTypeProvided(promotionType) && promotionSquare) {
            return MoveResult.promotionRequired();
        }

        PromotionType typeToUse = promotionSquare ? (promotionType != null ? promotionType : PromotionType.QUEEN) : null;

        try {
            pushState();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return MoveResult.illegal("Unable to save game state for undo");
        }

        int capturedCountBefore = capturedPieces.size();

        Piece movedPiece = performMoveInternal(piece, row, col, false, typeToUse);
        boolean promoted = piece instanceof Pawn && !(movedPiece instanceof Pawn);
        movedPiece.setHasMoved(true);
        setLastMovedPiece(movedPiece);
        switchTurn();
        recalculateAllMoves();
        updateGameState();

        boolean captureOccurred = capturedPieces.size() > capturedCountBefore;
        recordMoveNotation(piece, fromRow, fromCol, row, col, captureOccurred, promoted, movedPiece.getName());

        if (promoted) {
            return MoveResult.done("Pawn promoted to " + movedPiece.getName());
        }
        return MoveResult.done();
    }


    public boolean isEmpty(int row, int col){
        return board[row][col] == null;
    }


    public boolean isValidCapture(int row, int col, Piece piece) {
        if (row < 0 || row >= 8 || col < 0 || col >= 8) {
            return false;
        }
        Piece targetPiece = board[row][col];
        return targetPiece != null && targetPiece.isWhite() != piece.isWhite();
    }

    private boolean promotionTypeProvided(PromotionType promotionType) {
        return promotionType != null;
    }
    public void removePiece ( Piece piece){
        board [piece.getRow()][piece.getCol()] = null;
        for (int i = 0; i < activePieces.size(); i++) {
            if (activePieces.get(i) == piece) {
                activePieces.remove(i);
                break;
            }
        }

    }
    /**
     * Determines if a piece can be moved to a specific target location.
     * Checks if the target location is empty or occupied by a piece of
     * the opposite color.
     *
     * @param p the Piece object to be moved
     * @param targetRow the target row index to move the piece to
     * @param targetCol the target column index to move the piece to
     * @return true if the piece can be moved to the target location, false otherwise
     */
    public boolean moveLeavesKingInCheck(Piece p, int targetRow, int targetCol) throws FileNotFoundException {
        Board tempBoard = new Board(this);
        Piece tempPiece = tempBoard.getPiece(p.getRow(), p.getCol());
        if (tempPiece == null) {
            return false;
        }
        
        King king;
        if (p.isWhite()) {
            king = tempBoard.whiteKing;
        } else {
            king = tempBoard.blackKing;
        }
        
        if (king == null) {
            return false;
        }
        
        tempBoard.performMoveInternal(tempPiece, targetRow, targetCol, true, PromotionType.QUEEN);
        tempBoard.recalculateAllMoves();

        return king.isIncheck();
    }



    public void capture(Piece piece){
        capturedPieces.add(piece);
        activePieces.remove(piece);
        System.out.println(piece.getName() + " captured" + piece.getRow() + " " + piece.getCol());
        board[piece.getRow()][piece.getCol()] = null;
    }

    public boolean undo() throws FileNotFoundException {
        if (history.isEmpty()) {
            return false;
        }
        Board previous = history.pop();
        restoreFrom(previous);
        return true;
    }

    public void clearHistory() {
        history.clear();
    }

    public void reset() throws FileNotFoundException {
        board = new Piece[col][row];
        activePieces = new ArrayList<>();
        capturedPieces = new ArrayList<>();
        moveHistory = new ArrayList<>();
        setBoard();
        whiteKing = getKing(true);
        blackKing = getKing(false);
        enPassantTarget = null;
        whiteTurn = true;
        lastMovedPiece = null;
        selectedPiece = null;
        recalculateAllMoves();
        updateGameState();
        clearHistory();
    }

    private MoveResult validateMove(Piece piece, int targetRow, int targetCol) throws FileNotFoundException {
        if (isOutOfBounds(targetRow, targetCol)) {
            return MoveResult.illegal("Destination out of bounds");
        }

        if ((isWhiteTurn() && !piece.isWhite()) || (!isWhiteTurn() && piece.isWhite())) {
            return MoveResult.notYourTurn();
        }

        piece.calculateMoves();
        if (!piece.canMove(targetRow, targetCol)) {
            return MoveResult.illegal("Piece cannot move to that square");
        }

        if (!isEmpty(targetRow, targetCol) && !isValidCapture(targetRow, targetCol, piece)) {
            return MoveResult.illegal("Destination occupied by ally");
        }

        if (moveLeavesKingInCheck(piece, targetRow, targetCol)) {
            return MoveResult.leavesKingInCheck();
        }

        return MoveResult.done();
    }

    private boolean isOutOfBounds(int row, int col) {
        return row < 0 || row >= this.row || col < 0 || col >= this.col;
    }

    private Piece performMoveInternal(Piece piece, int targetRow, int targetCol, boolean simulation, PromotionType promotionType) throws FileNotFoundException {
        int originalRow = piece.getRow();
        int originalCol = piece.getCol();

        if (piece instanceof King && Math.abs(targetCol - originalCol) == 2) {
            boolean isKingSide = targetCol > originalCol;
            Castle((King) piece, isKingSide);
            enPassantTarget = null;
            return piece;
        }

        boolean isPawn = piece instanceof Pawn;
        Piece capturedPiece = board[targetRow][targetCol];
        int capturedRow = targetRow;

        if (isPawn && capturedPiece == null && originalCol != targetCol && enPassantTarget != null
                && enPassantTarget.getRow() == targetRow && enPassantTarget.getCol() == targetCol) {
            capturedRow = targetRow + (piece.isWhite() ? 1 : -1);
            capturedPiece = board[capturedRow][targetCol];
        }

        if (capturedPiece != null) {
            if (capturedPiece.isWhite() == piece.isWhite()) {
                throw new IllegalStateException("Attempted to capture allied piece");
            }
            if (simulation) {
                board[capturedRow][targetCol] = null;
                activePieces.remove(capturedPiece);
            } else {
                capture(capturedPiece);
            }
        }

        board[originalRow][originalCol] = null;
        board[targetRow][targetCol] = piece;
        piece.setCol(targetCol);
        piece.setRow(targetRow);
        piece.setX(targetCol * 100);
        piece.setY(targetRow * 100);

        Piece movedPiece = piece;

        if (isPawn && (targetRow == 0 || targetRow == 7)) {
            PromotionType type = promotionType != null ? promotionType : PromotionType.QUEEN;
            movedPiece = promotePawn((Pawn) piece, targetRow, targetCol, simulation, type);
        }

        if (isPawn && Math.abs(targetRow - originalRow) == 2) {
            int betweenRow = originalRow + (piece.isWhite() ? -1 : 1);
            enPassantTarget = new Position(betweenRow, targetCol);
        } else {
            enPassantTarget = null;
        }
        return movedPiece;
    }

    private Piece promotePawn(Pawn pawn, int targetRow, int targetCol, boolean simulation, PromotionType promotionType) throws FileNotFoundException {
        Piece promotedPiece;
        switch (promotionType) {
            case ROOK:
                promotedPiece = new Rook(targetRow, targetCol, pawn.isWhite(), this);
                break;
            case BISHOP:
                promotedPiece = new Bishop(targetRow, targetCol, pawn.isWhite(), this);
                break;
            case KNIGHT:
                promotedPiece = new Knight(targetRow, targetCol, pawn.isWhite(), this);
                break;
            case QUEEN:
            default:
                promotedPiece = new Queen(targetRow, targetCol, pawn.isWhite(), this);
                break;
        }
        promotedPiece.setHasMoved(true);
        promotedPiece.setX(targetCol * 100);
        promotedPiece.setY(targetRow * 100);

        // Replace pawn in active pieces list
        for (int i = 0; i < activePieces.size(); i++) {
            if (activePieces.get(i) == pawn) {
                activePieces.set(i, promotedPiece);
                break;
            }
        }

        board[targetRow][targetCol] = promotedPiece;

        if (!simulation) {
            System.out.println("Pawn promoted to Queen at " + targetRow + "," + targetCol);
        }

        return promotedPiece;
    }

    private void recalculateAllMoves() {
        for (Piece activePiece : activePieces) {
            activePiece.calculateMoves();
        }
    }

    private void pushState() throws FileNotFoundException {
        history.push(new Board(this));
    }

    private void restoreFrom(Board other) throws FileNotFoundException {
        Piece[][] newBoard = new Piece[col][row];
        ArrayList<Piece> newActive = new ArrayList<>();

        for (int i = 0; i < col; i++) {
            for (int j = 0; j < row; j++) {
                if (other.board[i][j] != null) {
                    Piece copy = other.board[i][j].DeepCopy(this);
                    copy.setRow(i);
                    copy.setCol(j);
                    copy.setX(j * 100);
                    copy.setY(i * 100);
                    newBoard[i][j] = copy;
                    newActive.add(copy);
                }
            }
        }

        this.board = newBoard;
        this.activePieces = newActive;
        this.capturedPieces = new ArrayList<>(other.capturedPieces);
        this.moveHistory = new ArrayList<>(other.moveHistory);
        this.whiteTurn = other.whiteTurn;
        this.gameState = other.gameState;
        this.enPassantTarget = other.enPassantTarget != null ? new Position(other.enPassantTarget.getRow(), other.enPassantTarget.getCol()) : null;
        this.selectedPiece = null;

        this.whiteKing = getKing(true);
        this.blackKing = getKing(false);

        if (other.lastMovedPiece != null) {
            this.lastMovedPiece = this.board[other.lastMovedPiece.getRow()][other.lastMovedPiece.getCol()];
        } else {
            this.lastMovedPiece = null;
        }

        recalculateAllMoves();
        updateGameState();
    }
    public void switchTurn(){
        whiteTurn = !whiteTurn;
    }
    public boolean isWhiteTurn(){
        return whiteTurn;
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
    public void setLastMovedPiece(Piece piece) {
        lastMovedPiece = piece;
    }
    public Piece getLastMovedPiece(){
        return lastMovedPiece;
    }

    public Piece getSelectedPiece() {
        return selectedPiece;
    }
    public ArrayList<Piece> getCapturedPiece() {
        return capturedPieces;
    }
    public Position getEnPassantTarget() {
        return enPassantTarget;
    }
    public ArrayList<String> getMoveHistory() {
        return moveHistory;
    }
    
    /**
     * Updates the game state based on check, checkmate, and stalemate conditions.
     * Note: After a move, turn switches, so we check the player whose turn it NOW is.
     */
    public void updateGameState() {
        if (whiteKing == null || blackKing == null) {
            return;
        }
        
        // After move, turn has switched, so check the player whose turn it is now
        if (isWhiteTurn()) {
            // It's white's turn - check if white is in check/checkmate/stalemate
            if (isCheckmate(true)) {
                gameState = GameState.WHITE_CHECKMATE;
            } else if (whiteKing.isIncheck()) {
                gameState = GameState.CHECK;
            } else if (isStalemate(true)) {
                gameState = GameState.STALEMATE;
            } else {
                gameState = GameState.PLAYING;
            }
        } else {
            // It's black's turn - check if black is in check/checkmate/stalemate
            if (isCheckmate(false)) {
                gameState = GameState.BLACK_CHECKMATE;
            } else if (blackKing.isIncheck()) {
                gameState = GameState.CHECK;
            } else if (isStalemate(false)) {
                gameState = GameState.STALEMATE;
            } else {
                gameState = GameState.PLAYING;
            }
        }
    }
    
    /**
     * Checks if the current player has any legal moves.
     * @param isWhite whether to check for white or black
     * @return true if the player has at least one legal move
     */
    private boolean hasLegalMoves(boolean isWhite) {
        for (Piece piece : activePieces) {
            if (piece.isWhite() == isWhite) {
                ArrayList<Move> moves = piece.getMoves();
                for (Move move : moves) {
                    // Check bounds
                    if (move.getRow() < 0 || move.getRow() >= 8 || 
                        move.getCol() < 0 || move.getCol() >= 8) {
                        continue;
                    }
                    
                    try {
                        // Check if move doesn't leave king in check
                        if (!moveLeavesKingInCheck(piece, move.getRow(), move.getCol())) {
                            // Verify the square is empty or contains an enemy piece
                            if (isEmpty(move.getRow(), move.getCol()) || 
                                isValidCapture(move.getRow(), move.getCol(), piece)) {
                                return true;
                            }
                        }
                    } catch (FileNotFoundException e) {
                        // Skip this move if there's an error
                        continue;
                    }
                }
            }
        }
        return false;
    }
    
    /**
     * Determines if the current player is in checkmate.
     * @param isWhite whether to check for white or black
     * @return true if the player is in checkmate
     */
    public boolean isCheckmate(boolean isWhite) {
        King king = isWhite ? whiteKing : blackKing;
        if (king == null) {
            return false;
        }
        
        // Must be in check
        if (!king.isIncheck()) {
            return false;
        }
        
        // Must have no legal moves
        return !hasLegalMoves(isWhite);
    }
    
    /**
     * Determines if the current player is in stalemate.
     * @param isWhite whether to check for white or black
     * @return true if the player is in stalemate
     */
    public boolean isStalemate(boolean isWhite) {
        King king = isWhite ? whiteKing : blackKing;
        if (king == null) {
            return false;
        }
        
        // Must NOT be in check
        if (king.isIncheck()) {
            return false;
        }
        
        // Must have no legal moves
        return !hasLegalMoves(isWhite);
    }
    
    /**
     * Gets the current game state.
     * @return the current GameState
     */
    public GameState getGameState() {
        return gameState;
    }
    
    /**
     * Enum representing the possible game states.
     */
    public enum GameState {
        PLAYING,
        CHECK,
        WHITE_CHECKMATE,
        BLACK_CHECKMATE,
        STALEMATE
    }

    private void recordMoveNotation(Piece piece, int fromRow, int fromCol, int toRow, int toCol,
                                    boolean capture, boolean promoted, String promotedPieceName) {
        boolean whiteMove = piece.isWhite();
        int moveNumber = (moveHistory.size() / 2) + 1;

        StringBuilder sb = new StringBuilder();
        if (whiteMove) {
            sb.append(moveNumber).append(". ");
        } else {
            sb.append(moveNumber).append("... ");
        }
        sb.append(piece.getName().charAt(0));
        sb.append(" ");
        sb.append(coordinateToString(fromRow, fromCol));
        sb.append(capture ? " x " : " -> ");
        sb.append(coordinateToString(toRow, toCol));
        if (promoted) {
            sb.append(" = ");
            sb.append(promotedPieceName.charAt(0));
        }
        moveHistory.add(sb.toString());
    }

    private String coordinateToString(int row, int col) {
        char file = (char) ('a' + col);
        int rank = 8 - row;
        return "" + file + rank;
    }
}

