package fr.astroware.chess.bot.rule;

import fr.astroware.chess.bot.api.BotContext;
import fr.astroware.chess.bot.evaluation.EvaluatedMove;

import java.util.List;

/**
 * Évalue les coups candidats correspondant à une situation détectée.
 *
 * <p>Une action ne retourne pas nécessairement un seul coup. Elle peut
 * proposer plusieurs candidats, chacun accompagné d'une note et d'une
 * explication. La règle retiendra ensuite le meilleur coup légal.</p>
 *
 * @param <D> type de détection attendu par l'action
 */
@FunctionalInterface
public interface Action<D extends Detection> {

    /**
     * Produit les coups candidats associés aux détections.
     *
     * @param context contexte courant du bot
     * @param detections occurrences reconnues par la situation
     * @return liste éventuellement vide de coups évalués
     */
    List<EvaluatedMove> evaluate(BotContext context, List<D> detections);
}
