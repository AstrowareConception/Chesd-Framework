package fr.astroware.chess.bot.situation.detection;

import fr.astroware.chess.bot.rule.Detection;
import fr.astroware.chess.core.model.Move;
import fr.astroware.chess.core.model.PlacedPiece;

import java.util.Objects;

/**
 * Coup créant une nouvelle pression sur le défenseur unique d'une cible.
 *
 * <p>Il s'agit d'une opportunité de déviation : le framework ne prétend pas
 * que la séquence est forcée sur plusieurs coups.</p>
 */
public record DeflectionDetection(
    Move move,
    PlacedPiece defender,
    PlacedPiece protectedTarget,
    int defenderValue,
    int targetValue,
    int addedAttackers
) implements Detection {

    public DeflectionDetection {
        Objects.requireNonNull(move, "move must not be null");
        Objects.requireNonNull(defender, "defender must not be null");
        Objects.requireNonNull(
            protectedTarget,
            "protectedTarget must not be null"
        );

        if (defenderValue < 0
            || targetValue < 0
            || addedAttackers <= 0) {
            throw new IllegalArgumentException(
                "deflection values are invalid"
            );
        }
    }
}
