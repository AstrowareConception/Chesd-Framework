package fr.astroware.chess.bot.rule;

import fr.astroware.chess.bot.api.BotContext;
import fr.astroware.chess.core.model.Move;

import java.util.List;
import java.util.Optional;

/**
 * Choisit un coup à partir des informations détectées par une situation.
 *
 * @param <D> type de détection attendu par l'action
 */
@FunctionalInterface
public interface Action<D extends Detection> {

    Optional<Move> choose(BotContext context, List<D> detections);
}
