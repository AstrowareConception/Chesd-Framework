package fr.astroware.chess.core.model;

import java.util.Objects;

/**
 * Une pièce d'échecs.
 *
 * @param color couleur de la pièce
 * @param type type de la pièce
 */
public record Piece(Color color, PieceType type) {

    public Piece {
        Objects.requireNonNull(color, "color must not be null");
        Objects.requireNonNull(type, "type must not be null");
    }
}
