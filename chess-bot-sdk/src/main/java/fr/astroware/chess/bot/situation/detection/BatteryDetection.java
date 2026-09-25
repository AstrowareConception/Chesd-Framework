package fr.astroware.chess.bot.situation.detection;

import fr.astroware.chess.bot.analysis.BatteryPattern;
import fr.astroware.chess.bot.rule.Detection;
import fr.astroware.chess.core.model.Move;

import java.util.Objects;

/**
 * Coup créant une nouvelle batterie.
 */
public record BatteryDetection(
    Move move,
    BatteryPattern pattern,
    int targetValue,
    boolean frontAttacked,
    boolean frontDefended
) implements Detection {

    public BatteryDetection {
        Objects.requireNonNull(move, "move must not be null");
        Objects.requireNonNull(pattern, "pattern must not be null");

        if (targetValue < 0) {
            throw new IllegalArgumentException(
                "targetValue must be non-negative"
            );
        }
    }
}
