package fr.astroware.chess.bot.plan;

import fr.astroware.chess.bot.evaluation.EvaluationScore;

import java.util.Objects;

/**
 * Avancement observable d'un plan stratégique.
 *
 * @param state état du plan
 * @param completion progression estimée de 0 à 10
 * @param explanation explication pédagogique
 */
public record PlanProgress(
    PlanState state,
    EvaluationScore completion,
    String explanation
) {

    public PlanProgress {
        Objects.requireNonNull(state, "state must not be null");
        Objects.requireNonNull(completion, "completion must not be null");
        explanation = Objects.requireNonNullElse(explanation, "");
    }

    public static PlanProgress active(double completion, String explanation) {
        return new PlanProgress(
            PlanState.ACTIVE,
            EvaluationScore.of(completion),
            explanation
        );
    }

    public static PlanProgress completed(String explanation) {
        return new PlanProgress(
            PlanState.COMPLETED,
            EvaluationScore.of(10.0),
            explanation
        );
    }

    public static PlanProgress blocked(double completion, String explanation) {
        return new PlanProgress(
            PlanState.BLOCKED,
            EvaluationScore.of(completion),
            explanation
        );
    }
}
