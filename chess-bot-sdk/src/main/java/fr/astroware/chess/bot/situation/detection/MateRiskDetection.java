package fr.astroware.chess.bot.situation.detection;

import fr.astroware.chess.bot.rule.Detection;
import fr.astroware.chess.core.model.Move;

import java.util.Objects;

/**
 * Coup légal qui autorise au moins un mat immédiat de l'adversaire.
 *
 * @param move coup du bot
 * @param opponentMateReplies nombre de réponses adverses donnant mat
 */
public record MateRiskDetection(
    Move move,
    int opponentMateReplies
) implements Detection {

    public MateRiskDetection {
        Objects.requireNonNull(move, "move must not be null");

        if (opponentMateReplies <= 0) {
            throw new IllegalArgumentException(
                "opponentMateReplies must be > 0"
            );
        }
    }
}
