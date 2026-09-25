package fr.astroware.chess.bot.situation.detection;

import fr.astroware.chess.bot.rule.Detection;
import fr.astroware.chess.core.model.PlacedPiece;

import java.util.Objects;

/**
 * Décrit une pièce alliée menacée.
 *
 * @param piece pièce concernée
 * @param value valeur matérielle
 * @param attackers nombre d'attaquants adverses
 * @param defenders nombre de défenseurs alliés
 */
public record ThreatenedPieceDetection(
    PlacedPiece piece,
    int value,
    int attackers,
    int defenders
) implements Detection {

    public ThreatenedPieceDetection {
        Objects.requireNonNull(piece, "piece must not be null");

        if (value < 0 || attackers < 0 || defenders < 0) {
            throw new IllegalArgumentException(
                "Threat values must be non-negative"
            );
        }
    }
}
