package fr.astroware.chess.bot.situation.detection;

import fr.astroware.chess.bot.rule.Detection;
import fr.astroware.chess.core.model.Move;
import fr.astroware.chess.core.model.PieceType;

import java.util.Objects;

/**
 * Coup légal réalisant une promotion de pion.
 *
 * @param move coup de promotion
 * @param promotedTo pièce choisie
 */
public record PromotionDetection(
    Move move,
    PieceType promotedTo
) implements Detection {

    public PromotionDetection {
        Objects.requireNonNull(
            move,
            "move must not be null"
        );
        Objects.requireNonNull(
            promotedTo,
            "promotedTo must not be null"
        );

        if (move.promotion().isEmpty()) {
            throw new IllegalArgumentException(
                "PromotionDetection requires a promotion move"
            );
        }

        if (move.promotion().orElseThrow()
            != promotedTo) {
            throw new IllegalArgumentException(
                "promotedTo must match move promotion"
            );
        }
    }
}
