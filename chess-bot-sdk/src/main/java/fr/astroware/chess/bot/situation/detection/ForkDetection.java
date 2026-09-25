package fr.astroware.chess.bot.situation.detection;

import fr.astroware.chess.bot.rule.Detection;
import fr.astroware.chess.core.model.Move;
import fr.astroware.chess.core.model.PlacedPiece;

import java.util.List;
import java.util.Objects;

/**
 * Fourchette créée par un coup.
 *
 * @param move coup créant la fourchette
 * @param attacker pièce après son déplacement
 * @param targets pièces adverses attaquées simultanément
 * @param targetValueSum somme des valeurs matérielles des cibles
 * @param attackerAttacked vrai si l'attaquant est lui-même attaqué après le coup
 * @param attackerDefended vrai si l'attaquant est défendu après le coup
 */
public record ForkDetection(
    Move move,
    PlacedPiece attacker,
    List<PlacedPiece> targets,
    int targetValueSum,
    boolean attackerAttacked,
    boolean attackerDefended
) implements Detection {

    public ForkDetection {
        Objects.requireNonNull(move, "move must not be null");
        Objects.requireNonNull(attacker, "attacker must not be null");
        targets = List.copyOf(
            Objects.requireNonNull(targets, "targets must not be null")
        );

        if (targets.size() < 2) {
            throw new IllegalArgumentException(
                "A fork must attack at least two pieces"
            );
        }

        if (targetValueSum < 0) {
            throw new IllegalArgumentException(
                "targetValueSum must be non-negative"
            );
        }
    }
}
