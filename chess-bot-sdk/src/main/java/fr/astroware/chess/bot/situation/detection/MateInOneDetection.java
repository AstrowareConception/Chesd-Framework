package fr.astroware.chess.bot.situation.detection;

import fr.astroware.chess.bot.rule.Detection;
import fr.astroware.chess.core.model.Move;

import java.util.Objects;

/**
 * Coup qui termine immédiatement la partie par échec et mat.
 */
public record MateInOneDetection(Move move) implements Detection {

    public MateInOneDetection {
        Objects.requireNonNull(move, "move must not be null");
    }
}
