package fr.astroware.chess.core.model;

import java.util.Objects;

/**
 * Une pièce associée à sa position sur l'échiquier.
 *
 * @param piece pièce
 * @param square case occupée
 */
public record PlacedPiece(Piece piece, Square square) {

    public PlacedPiece {
        Objects.requireNonNull(piece, "piece must not be null");
        Objects.requireNonNull(square, "square must not be null");
    }
}
