package fr.astroware.chess.bot.evaluation;

import fr.astroware.chess.bot.api.BotContext;

/**
 * Stratégie d'évaluation d'une position.
 *
 * <p>Un étudiant pourra utiliser l'évaluateur fourni par le framework,
 * modifier les poids de ses critères ou écrire sa propre implémentation.</p>
 */
@FunctionalInterface
public interface PositionEvaluator {

    PositionEvaluation evaluate(BotContext context);
}
