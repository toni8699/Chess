package Main;

public class MoveResult {

    private final MoveStatus status;
    private final String message;
    private final boolean promotionRequest;

    public MoveResult(MoveStatus status, String message) {
        this(status, message, false);
    }

    public MoveResult(MoveStatus status, String message, boolean promotionRequest) {
        this.status = status;
        this.message = message;
        this.promotionRequest = promotionRequest;
    }

    public MoveStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public boolean isDone() {
        return status == MoveStatus.DONE;
    }

    public static MoveResult done() {
        return new MoveResult(MoveStatus.DONE, null);
    }

    public static MoveResult done(String message) {
        return new MoveResult(MoveStatus.DONE, message);
    }

    public static MoveResult illegal(String message) {
        return new MoveResult(MoveStatus.ILLEGAL_MOVE, message);
    }

    public static MoveResult leavesKingInCheck() {
        return new MoveResult(MoveStatus.LEAVES_KING_IN_CHECK, "Move leaves king in check");
    }

    public static MoveResult notYourTurn() {
        return new MoveResult(MoveStatus.NOT_YOUR_TURN, "It is not this piece's turn");
    }

    public static MoveResult promotionRequired() {
        return new MoveResult(MoveStatus.PROMOTION_REQUIRED, "Select a piece for promotion", true);
    }

    public boolean isPromotionRequest() {
        return promotionRequest;
    }
}

