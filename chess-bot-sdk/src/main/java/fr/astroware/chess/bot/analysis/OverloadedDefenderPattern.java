package fr.astroware.chess.bot.analysis;

import fr.astroware.chess.core.model.PlacedPiece;

import java.util.List;
import java.util.Objects;

/**
 * Défenseur surchargé : une même pièce est l'unique défenseur d'au moins deux
 * cibles actuellement attaquées.
 *
 * @param defender pièce surchargée
 * @param protectedTargets cibles dont elle est l'unique défenseur
 */
public record OverloadedDefenderPattern(
    PlacedPiece defender,
    List<PlacedPiece> protectedTargets
) {

    public OverloadedDefenderPattern {
        Objects.requireNonNull(defender, "defender must not be null");
        protectedTargets = List.copyOf(
            Objects.requireNonNull(
                protectedTargets,
                "protectedTargets must not be null"
            )
        );

        if (protectedTargets.size() < 2) {
            throw new IllegalArgumentException(
                "An overloaded defender must uniquely defend at least two targets"
            );
        }
    }
}
