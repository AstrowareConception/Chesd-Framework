package fr.astroware.chess.bot.plan;

import fr.astroware.chess.bot.api.BotContext;
import fr.astroware.chess.bot.evaluation.EvaluatedMove;
import fr.astroware.chess.bot.rule.PresenceDetection;
import fr.astroware.chess.bot.rule.Rule;
import fr.astroware.chess.bot.situation.Situations;

import java.util.List;

/**
 * Objectif stratégique susceptible de demander plusieurs coups.
 *
 * <p>Contrairement à une ouverture, un plan n'impose pas une suite exacte de
 * coups. À chaque tour, il réévalue la position et propose les coups qui font
 * progresser l'objectif.</p>
 */
public interface StrategicPlan {

    String name();

    String description();

    PlanProgress progress(BotContext context);

    List<EvaluatedMove> candidates(BotContext context);

    /**
     * Transforme le plan en règle directement insérable dans la liste du bot.
     */
    default Rule<PresenceDetection> asRule() {
        return Rule.of(
            "Plan : " + name(),
            Situations.always(),
            (context, detections) -> {
                if (progress(context).state() == PlanState.COMPLETED) {
                    return List.of();
                }

                return candidates(context);
            }
        );
    }
}
