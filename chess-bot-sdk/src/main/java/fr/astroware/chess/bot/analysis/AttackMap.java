package fr.astroware.chess.bot.analysis;

import fr.astroware.chess.core.model.Color;
import fr.astroware.chess.core.model.PlacedPiece;
import fr.astroware.chess.core.model.Square;

import java.util.List;
import java.util.Set;

/**
 * Carte des cases attaquées par les pièces présentes sur l'échiquier.
 *
 * <p>Une attaque est ici une relation géométrique conforme au déplacement de
 * la pièce. Pour les pièces coulissantes, le premier obstacle est attaqué puis
 * bloque la suite du rayon.</p>
 */
public interface AttackMap {

    Set<Square> attackedBy(Color color);

    List<PlacedPiece> attackersOf(Square square, Color color);

    int attackCount(Square square, Color color);

    Set<Square> attacksFrom(PlacedPiece piece);

    default boolean isAttacked(Square square, Color byColor) {
        return attackCount(square, byColor) > 0;
    }
}
