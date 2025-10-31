package Main;

public enum PromotionType {
    QUEEN,
    ROOK,
    BISHOP,
    KNIGHT;

    public static PromotionType fromString(String value) {
        if (value == null) {
            return null;
        }
        switch (value.toLowerCase()) {
            case "queen":
                return QUEEN;
            case "rook":
                return ROOK;
            case "bishop":
                return BISHOP;
            case "knight":
                return KNIGHT;
            default:
                return null;
        }
    }

    @Override
    public String toString() {
        String name = name().toLowerCase();
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }
}

