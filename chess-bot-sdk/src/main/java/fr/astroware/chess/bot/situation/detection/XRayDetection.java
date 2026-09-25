package fr.astroware.chess.bot.situation.detection;

import fr.astroware.chess.bot.analysis.XRayPattern;
import fr.astroware.chess.bot.rule.Detection;
import fr.astroware.chess.core.model.Move;

import java.util.Objects;

/**
 * Coup créant une nouvelle pression de rayon X.
 */
public record XRayDetection(
    Move move,
    XRayPattern pattern,
    int blockerValue,
    int targetValue,
    boolean attackerAttacked,
    boolean attackerDefended
) implements Detection {

    public XRayDetection {
        Objects.requireNonNull(move, "move must not be null");
        Objects.requireNonNull(pattern, "pattern must not be null");

        if (blockerValue < 0 || targetValue < 0) {
            throw new IllegalArgumentException(
                "x-ray values must be non-negative"
            );
        }
    }
}
