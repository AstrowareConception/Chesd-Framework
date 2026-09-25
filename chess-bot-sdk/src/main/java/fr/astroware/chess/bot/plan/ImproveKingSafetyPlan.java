package fr.astroware.chess.bot.plan;

import fr.astroware.chess.bot.analysis.PositionProjection;
import fr.astroware.chess.bot.api.BotContext;
import fr.astroware.chess.bot.evaluation.EvaluatedMove;
import fr.astroware.chess.core.model.Move;

import java.util.List;
import java.util.Optional;

/**
 * Plan générique visant à améliorer la sécurité du roi.
 */
final class ImproveKingSafetyPlan implements StrategicPlan {

    @Override
    public String name() {
        return "Améliorer la sécurité du roi";
    }

    @Override
    public String description() {
        return "Choisir des coups qui réduisent la pression autour du roi.";
    }

    @Override
    public PlanProgress progress(BotContext context) {
        double safety =
            context.analysis().kingSafetyScore(
                context.myColor()
            );

        if (safety >= 8.5) {
            return PlanProgress.completed(
                "La sécurité du roi est déjà élevée : "
                    + String.format(
                        java.util.Locale.ROOT,
                        "%.1f/10",
                        safety
                    )
            );
        }

        return PlanProgress.active(
            safety,
            "Sécurité actuelle du roi : "
                + String.format(
                    java.util.Locale.ROOT,
                    "%.1f/10",
                    safety
                )
        );
    }

    @Override
    public List<EvaluatedMove> candidates(BotContext context) {
        double before =
            context.analysis().kingSafetyScore(
                context.myColor()
            );

        return context.legalMoves().stream()
            .map(move -> candidate(context, move, before))
            .flatMap(Optional::stream)
            .toList();
    }

    private Optional<EvaluatedMove> candidate(
        BotContext context,
        Move move,
        double before
    ) {
        PositionProjection projection =
            context.analysis().after(move);

        double after =
            projection.analysis().kingSafetyScore(
                context.myColor()
            );

        if (after <= before + 0.25) {
            return Optional.empty();
        }

        double positional =
            projection.analysis()
                .positionEvaluation(context.myColor())
                .total();

        double score = Math.clamp(
            Math.max(after, positional),
            0.0,
            9.4
        );

        return Optional.of(
            EvaluatedMove.strategic(
                move,
                score,
                2.5,
                after,
                10.0 - after,
                "Sécurité du roi : "
                    + String.format(
                        java.util.Locale.ROOT,
                        "%.1f -> %.1f",
                        before,
                        after
                    )
            )
        );
    }
}
