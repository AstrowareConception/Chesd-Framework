package fr.astroware.chess.bot.analysis;

import fr.astroware.chess.core.model.PlacedPiece;

import java.util.Objects;

/**
 * Enfilade observée dans une position.
 *
 * <p>Une pièce coulissante attaque une pièce de forte valeur située devant
 * une seconde cible sur le même rayon.</p>
 *
 * @param attacker pièce qui exerce l'enfilade
 * @param front pièce directement attaquée
 * @param rear pièce située derrière
 */
public record SkewerPattern(
    PlacedPiece attacker,
    PlacedPiece front,
    PlacedPiece rear
) {

    public SkewerPattern {
        Objects.requireNonNull(attacker, "attacker must not be null");
        Objects.requireNonNull(front, "front must not be null");
        Objects.requireNonNull(rear, "rear must not be null");
    }
}
