package fr.astroware.chess.bot.plan;

import fr.astroware.chess.bot.analysis.PawnStructure;
import fr.astroware.chess.bot.analysis.PositionProjection;
import fr.astroware.chess.bot.api.BotContext;
import fr.astroware.chess.bot.evaluation.EvaluatedMove;
import fr.astroware.chess.core.model.Move;

import java.util.List;

/**
 * Plan visant à créer au moins un pion passé.
 */
final class CreatePassedPawnPlan implements StrategicPlan {

    @Override
    public String name() {
        return "Créer un pion passé";
    }

    @Override
    public String description() {
        return "Chercher les transformations de structure qui créent un pion passé.";
    }

    @Override
    public PlanProgress progress(BotContext context) {
        PawnStructure structure =
            context.analysis().pawnStructure(context.myColor());

        if (!structure.passed().isEmpty()) {
            return PlanProgress.completed(
                "Le camp possède déjà "
                    + structure.passedCount()
                    + " pion(s) passé(s)."
            );
        }

        double completion = Math.clamp(
            4.0
                + structure.pawns().size() * 0.25
                - structure.isolatedCount() * 0.2,
            0.0,
            8.0
        );

        return PlanProgress.active(
            completion,
            "Aucun pion passé pour l'instant."
        );
    }

    @Override
    public List<EvaluatedMove> candidates(BotContext context) {
        PawnStructure before =
            context.analysis().pawnStructure(context.myColor());

        return context.legalMoves().stream()
            .map(move -> candidate(context, before, move))
            .flatMap(java.util.Optional::stream)
            .toList();
    }

    private java.util.Optional<EvaluatedMove> candidate(
        BotContext context,
        PawnStructure before,
        Move move
    ) {
        PositionProjection projection =
            context.analysis().after(move);

        PawnStructure after =
            projection.analysis().pawnStructure(context.myColor());

        int newPassed =
            after.passedCount() - before.passedCount();

        int newProtected =
            after.protectedPassed().size()
                - before.protectedPassed().size();

        if (newPassed <= 0 && newProtected <= 0) {
            return java.util.Optional.empty();
        }

        double positional =
            projection.analysis()
                .positionEvaluation(context.myColor())
                .total();

        double score = Math.clamp(
            Math.max(7.0, positional)
                + newPassed * 0.8
                + newProtected * 0.4,
            0.0,
            9.6
        );

        double safety =
            projection.analysis().kingSafetyScore(
                context.myColor()
            );

        return java.util.Optional.of(
            EvaluatedMove.strategic(
                move,
                score,
                5.5,
                safety,
                10.0 - safety,
                "Crée "
                    + newPassed
                    + " pion(s) passé(s) et "
                    + newProtected
                    + " pion(s) passé(s) protégé(s)"
            )
        );
    }
}
