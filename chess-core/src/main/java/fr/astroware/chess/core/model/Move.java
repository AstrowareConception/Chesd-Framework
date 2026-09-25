package fr.astroware.chess.core.model;

import java.util.Objects;
import java.util.Optional;

/**
 * Déplacement proposé sur l'échiquier.
 *
 * <p>La légalité complète d'un coup n'est pas la responsabilité de cet objet.
 * Elle appartient au moteur de règles. {@code Move} décrit uniquement une
 * intention de déplacement.</p>
 *
 * @param from case de départ
 * @param to case d'arrivée
 * @param promotion type de promotion éventuel
 */
public record Move(
    Square from,
    Square to,
    Optional<PieceType> promotion
) {

    public Move {
        Objects.requireNonNull(from, "from must not be null");
        Objects.requireNonNull(to, "to must not be null");
        Objects.requireNonNull(promotion, "promotion must not be null");

        promotion.ifPresent(type -> {
            if (type == PieceType.KING || type == PieceType.PAWN) {
                throw new IllegalArgumentException(
                    "A pawn cannot be promoted to " + type
                );
            }
        });
    }

    public static Move of(String from, String to) {
        return new Move(
            Square.from(from),
            Square.from(to),
            Optional.empty()
        );
    }

    public static Move promotion(String from, String to, PieceType pieceType) {
        return new Move(
            Square.from(from),
            Square.from(to),
            Optional.of(Objects.requireNonNull(pieceType))
        );
    }
}
