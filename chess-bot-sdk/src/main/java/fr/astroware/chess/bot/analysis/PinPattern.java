package fr.astroware.chess.bot.analysis;

import fr.astroware.chess.core.model.PlacedPiece;

import java.util.Objects;

/**
 * Clouage absolu observé dans une position.
 *
 * <p>La pièce {@code pinned} se trouve entre une pièce coulissante adverse
 * et son propre roi. La déplacer hors de la ligne exposerait donc le roi.</p>
 *
 * @param attacker pièce qui exerce le clouage
 * @param pinned pièce clouée
 * @param king roi situé derrière la pièce clouée
 */
public record PinPattern(
    PlacedPiece attacker,
    PlacedPiece pinned,
    PlacedPiece king
) {

    public PinPattern {
        Objects.requireNonNull(attacker, "attacker must not be null");
        Objects.requireNonNull(pinned, "pinned must not be null");
        Objects.requireNonNull(king, "king must not be null");
    }
}
