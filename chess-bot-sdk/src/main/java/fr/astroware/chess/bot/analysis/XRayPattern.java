package fr.astroware.chess.bot.analysis;

import fr.astroware.chess.core.model.PlacedPiece;

import java.util.Objects;

/**
 * Pression de rayon X : une pièce coulissante vise une seconde cible adverse
 * à travers une première pièce adverse.
 *
 * @param attacker pièce coulissante
 * @param blocker première pièce adverse sur le rayon
 * @param target seconde cible adverse derrière le bloqueur
 */
public record XRayPattern(
    PlacedPiece attacker,
    PlacedPiece blocker,
    PlacedPiece target
) {

    public XRayPattern {
        Objects.requireNonNull(attacker, "attacker must not be null");
        Objects.requireNonNull(blocker, "blocker must not be null");
        Objects.requireNonNull(target, "target must not be null");
    }
}
