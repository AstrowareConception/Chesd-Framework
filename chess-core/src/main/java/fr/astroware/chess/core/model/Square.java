package fr.astroware.chess.core.model;

import java.util.Locale;
import java.util.Objects;

/**
 * Case d'un échiquier.
 *
 * <p>Une case est un objet valeur immuable. Ainsi, deux instances
 * représentant e4 sont égales, ce qui simplifie les comparaisons dans
 * l'ensemble du framework.</p>
 *
 * @param file colonne de a à h
 * @param rank rangée de 1 à 8
 */
public record Square(BoardFile file, BoardRank rank) {

    public Square {
        Objects.requireNonNull(file, "file must not be null");
        Objects.requireNonNull(rank, "rank must not be null");
    }

    /**
     * Construit une case depuis la notation algébrique usuelle.
     *
     * @param notation notation telle que "e4" ou "A8"
     * @return la case correspondante
     */
    public static Square from(String notation) {
        Objects.requireNonNull(notation, "notation must not be null");

        String normalized = notation.trim().toLowerCase(Locale.ROOT);

        if (normalized.length() != 2) {
            throw new IllegalArgumentException(
                "A square must use algebraic notation such as e4: " + notation
            );
        }

        char file = normalized.charAt(0);
        char rank = normalized.charAt(1);

        if (!Character.isDigit(rank)) {
            throw new IllegalArgumentException("Invalid square: " + notation);
        }

        return new Square(
            BoardFile.from(file),
            BoardRank.from(Character.digit(rank, 10))
        );
    }

    /**
     * Retourne la notation algébrique de la case.
     */
    public String notation() {
        return "" + file.symbol() + rank.number();
    }

    @Override
    public String toString() {
        return notation();
    }
}
