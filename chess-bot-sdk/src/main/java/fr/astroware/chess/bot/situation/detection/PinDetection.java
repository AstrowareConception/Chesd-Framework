package fr.astroware.chess.bot.situation.detection;

import fr.astroware.chess.bot.analysis.PinPattern;
import fr.astroware.chess.bot.rule.Detection;
import fr.astroware.chess.core.model.Move;

import java.util.Objects;

/**
 * Coup créant un nouveau clouage absolu.
 *
 * @param move coup créant le motif
 * @param pattern clouage obtenu
 * @param pinnedValue valeur matérielle de la pièce clouée
 * @param attackerAttacked vrai si l'attaquant est attaqué après le coup
 * @param attackerDefended vrai s'il est défendu après le coup
 */
public record PinDetection(
    Move move,
    PinPattern pattern,
    int pinnedValue,
    boolean attackerAttacked,
    boolean attackerDefended
) implements Detection {

    public PinDetection {
        Objects.requireNonNull(move, "move must not be null");
        Objects.requireNonNull(pattern, "pattern must not be null");

        if (pinnedValue < 0) {
            throw new IllegalArgumentException(
                "pinnedValue must be non-negative"
            );
        }
    }
}
