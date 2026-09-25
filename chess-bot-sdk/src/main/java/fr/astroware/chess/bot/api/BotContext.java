package fr.astroware.chess.bot.api;

import fr.astroware.chess.core.model.Color;
import fr.astroware.chess.core.model.Move;
import fr.astroware.chess.core.model.PositionView;

import java.util.List;
import java.util.random.RandomGenerator;

/**
 * Informations accessibles à un bot au moment où il doit jouer.
 *
 * <p>Le contexte est en lecture seule. Le bot peut observer la position et
 * choisir parmi les coups légaux, mais il ne peut pas modifier directement
 * l'état interne de la partie.</p>
 */
public interface BotContext {

    Color myColor();

    PositionView position();

    List<Move> legalMoves();

    RandomGenerator random();
}
