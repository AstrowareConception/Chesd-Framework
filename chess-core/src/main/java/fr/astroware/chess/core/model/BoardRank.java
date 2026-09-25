package fr.astroware.chess.core.model;

/**
 * Rangée d'un échiquier, de 1 à 8.
 */
public enum BoardRank {
    ONE(1),
    TWO(2),
    THREE(3),
    FOUR(4),
    FIVE(5),
    SIX(6),
    SEVEN(7),
    EIGHT(8);

    private final int number;

    BoardRank(int number) {
        this.number = number;
    }

    public int number() {
        return number;
    }

    public static BoardRank from(int number) {
        for (BoardRank rank : values()) {
            if (rank.number == number) {
                return rank;
            }
        }

        throw new IllegalArgumentException("Unknown board rank: " + number);
    }
}
