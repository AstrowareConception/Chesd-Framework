package fr.astroware.chess.bot.analysis;

import fr.astroware.chess.core.game.GameResult;
import fr.astroware.chess.core.model.Move;
import fr.astroware.chess.core.model.PositionView;

import java.util.List;
import java.util.Objects;

/**
 * Résultat de la simulation d'un coup.
 *
 * <p>La projection est totalement indépendante de la vraie partie : le moteur
 * de règles construit une nouvelle position puis l'analyse comme n'importe
 * quelle autre position.</p>
 *
 * @param position position obtenue après le coup
 * @param analysis analyse de cette nouvelle position
 * @param legalMoves coups légaux du camp désormais au trait
 * @param result résultat éventuel de la partie
 */
public record PositionProjection(
    PositionView position,
    Analysis analysis,
    List<Move> legalMoves,
    GameResult result
) {

    public PositionProjection {
        Objects.requireNonNull(position, "position must not be null");
        Objects.requireNonNull(analysis, "analysis must not be null");
        legalMoves = List.copyOf(
            Objects.requireNonNull(legalMoves, "legalMoves must not be null")
        );
        Objects.requireNonNull(result, "result must not be null");
    }
}
