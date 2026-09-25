package fr.astroware.chess.bot.situation.detection;

import fr.astroware.chess.bot.rule.Detection;
import fr.astroware.chess.core.model.Move;

import java.util.Objects;

/**
 * Coup légal de roque.
 *
 * @param move déplacement du roi
 * @param kingSide vrai pour le petit roque, faux pour le grand roque
 */
public record CastlingDetection(
    Move move,
    boolean kingSide
) implements Detection {

    public CastlingDetection {
        Objects.requireNonNull(
            move,
            "move must not be null"
        );
    }

    public String sideName() {
        return kingSide
            ? "petit roque"
            : "grand roque";
    }
}
