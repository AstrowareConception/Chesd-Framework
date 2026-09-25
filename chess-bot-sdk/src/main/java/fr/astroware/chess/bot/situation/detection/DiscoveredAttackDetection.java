package fr.astroware.chess.bot.situation.detection;

import fr.astroware.chess.bot.rule.Detection;
import fr.astroware.chess.core.model.Move;
import fr.astroware.chess.core.model.PlacedPiece;

import java.util.Objects;

/**
 * Attaque révélée par le déplacement d'une autre pièce.
 *
 * @param move coup qui libère la ligne
 * @param revealedAttacker pièce dont l'attaque est découverte
 * @param target nouvelle cible attaquée
 * @param targetValue valeur tactique de la cible
 * @param movedPieceAttacked vrai si la pièce déplacée est attaquée après le coup
 * @param movedPieceDefended vrai si elle est défendue après le coup
 */
public record DiscoveredAttackDetection(
    Move move,
    PlacedPiece revealedAttacker,
    PlacedPiece target,
    int targetValue,
    boolean movedPieceAttacked,
    boolean movedPieceDefended
) implements Detection {

    public DiscoveredAttackDetection {
        Objects.requireNonNull(move, "move must not be null");
        Objects.requireNonNull(
            revealedAttacker,
            "revealedAttacker must not be null"
        );
        Objects.requireNonNull(target, "target must not be null");

        if (targetValue < 0) {
            throw new IllegalArgumentException(
                "targetValue must be non-negative"
            );
        }
    }
}
