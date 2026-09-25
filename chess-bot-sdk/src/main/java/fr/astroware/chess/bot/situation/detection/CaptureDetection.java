package fr.astroware.chess.bot.situation.detection;

import fr.astroware.chess.bot.rule.Detection;
import fr.astroware.chess.core.model.Move;
import fr.astroware.chess.core.model.PlacedPiece;

import java.util.Objects;

/**
 * Décrit une capture légale détectée dans la position courante.
 *
 * @param move coup de capture
 * @param attacker pièce qui effectue la capture
 * @param target pièce capturée
 * @param attackerValue valeur matérielle de l'attaquant
 * @param targetValue valeur matérielle de la cible
 * @param targetDefenders nombre de pièces adverses défendant la case cible
 */
public record CaptureDetection(
    Move move,
    PlacedPiece attacker,
    PlacedPiece target,
    int attackerValue,
    int targetValue,
    int targetDefenders
) implements Detection {

    public CaptureDetection {
        Objects.requireNonNull(move, "move must not be null");
        Objects.requireNonNull(attacker, "attacker must not be null");
        Objects.requireNonNull(target, "target must not be null");

        if (attackerValue < 0 || targetValue < 0 || targetDefenders < 0) {
            throw new IllegalArgumentException(
                "Capture numeric values must be non-negative"
            );
        }
    }

    public boolean targetIsHanging() {
        return targetDefenders == 0;
    }
}
