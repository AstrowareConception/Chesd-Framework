package fr.astroware.chess.bot.analysis;

import fr.astroware.chess.core.model.PlacedPiece;

import java.util.Objects;

/**
 * Batterie de deux pièces coulissantes alliées alignées sur une cible adverse.
 *
 * @param rear pièce située derrière
 * @param front pièce alliée située devant sur le même rayon
 * @param target cible adverse située au-delà de la pièce avant
 */
public record BatteryPattern(
    PlacedPiece rear,
    PlacedPiece front,
    PlacedPiece target
) {

    public BatteryPattern {
        Objects.requireNonNull(rear, "rear must not be null");
        Objects.requireNonNull(front, "front must not be null");
        Objects.requireNonNull(target, "target must not be null");
    }
}
