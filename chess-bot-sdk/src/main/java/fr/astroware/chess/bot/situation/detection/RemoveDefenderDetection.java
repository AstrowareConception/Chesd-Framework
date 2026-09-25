package fr.astroware.chess.bot.situation.detection;

import fr.astroware.chess.bot.analysis.OverloadedDefenderPattern;
import fr.astroware.chess.bot.rule.Detection;
import fr.astroware.chess.core.model.Move;
import fr.astroware.chess.core.model.PlacedPiece;

import java.util.List;
import java.util.Objects;

/**
 * Capture d'un défenseur surchargé qui expose de nouvelles cibles.
 *
 * @param move coup éliminant le défenseur
 * @param overload surcharge identifiée avant le coup
 * @param newlyHangingTargets cibles devenues pendues après la capture
 * @param exposedValue somme de leur valeur matérielle
 */
public record RemoveDefenderDetection(
    Move move,
    OverloadedDefenderPattern overload,
    List<PlacedPiece> newlyHangingTargets,
    int exposedValue
) implements Detection {

    public RemoveDefenderDetection {
        Objects.requireNonNull(move, "move must not be null");
        Objects.requireNonNull(overload, "overload must not be null");
        newlyHangingTargets = List.copyOf(
            Objects.requireNonNull(
                newlyHangingTargets,
                "newlyHangingTargets must not be null"
            )
        );

        if (newlyHangingTargets.isEmpty()) {
            throw new IllegalArgumentException(
                "Removing the defender must expose at least one target"
            );
        }

        if (exposedValue < 0) {
            throw new IllegalArgumentException(
                "exposedValue must be non-negative"
            );
        }
    }
}
