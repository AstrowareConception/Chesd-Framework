package fr.astroware.chess.core.model;

/**
 * Colonne d'un échiquier, de a à h.
 *
 * <p>Le terme {@code BoardFile} est préféré à {@code File} afin d'éviter
 * toute confusion avec {@code java.io.File}.</p>
 */
public enum BoardFile {
    A('a'),
    B('b'),
    C('c'),
    D('d'),
    E('e'),
    F('f'),
    G('g'),
    H('h');

    private final char symbol;

    BoardFile(char symbol) {
        this.symbol = symbol;
    }

    public char symbol() {
        return symbol;
    }

    public static BoardFile from(char symbol) {
        char normalized = Character.toLowerCase(symbol);

        for (BoardFile file : values()) {
            if (file.symbol == normalized) {
                return file;
            }
        }

        throw new IllegalArgumentException("Unknown board file: " + symbol);
    }
}
