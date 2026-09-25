package fr.astroware.chess.bot.situation.detection;

import fr.astroware.chess.bot.analysis.SkewerPattern;
import fr.astroware.chess.bot.rule.Detection;
import fr.astroware.chess.core.model.Move;

import java.util.Objects;

/**
 * Coup créant une nouvelle enfilade.
 *
 * @param move coup créant le motif
 * @param pattern enfilade obtenue
 * @param frontValue valeur tactique de la pièce située devant
 * @param rearValue valeur de la cible située derrière
 * @param attackerAttacked vrai si l'attaquant est attaqué après le coup
 * @param attackerDefended vrai s'il est défendu après le coup
 */
public record SkewerDetection(
    Move move,
    SkewerPattern pattern,
    int frontValue,
    int rearValue,
    boolean attackerAttacked,
    boolean attackerDefended
) implements Detection {

    public SkewerDetection {
        Objects.requireNonNull(move, "move must not be null");
        Objects.requireNonNull(pattern, "pattern must not be null");

        if (frontValue < 0 || rearValue < 0) {
            throw new IllegalArgumentException(
                "skewer values must be non-negative"
            );
        }
    }
}
