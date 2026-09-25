package fr.astroware.chess.bot.situation.detection;

import fr.astroware.chess.bot.rule.Detection;
import fr.astroware.chess.core.model.Move;

import java.util.Objects;

/**
 * Coup légal donnant échec au roi adverse.
 *
 * @param move coup donnant échec
 * @param opponentReplies nombre de réponses légales laissées à l'adversaire
 * @param movedPieceAttacked vrai si la pièce déplacée est attaquée après le coup
 * @param movedPieceDefended vrai si elle est défendue après le coup
 */
public record CheckingMoveDetection(
    Move move,
    int opponentReplies,
    boolean movedPieceAttacked,
    boolean movedPieceDefended
) implements Detection {

    public CheckingMoveDetection {
        Objects.requireNonNull(move, "move must not be null");

        if (opponentReplies < 0) {
            throw new IllegalArgumentException(
                "opponentReplies must be non-negative"
            );
        }
    }
}
