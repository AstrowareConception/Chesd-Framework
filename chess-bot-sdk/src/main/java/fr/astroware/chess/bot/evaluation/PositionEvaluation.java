package fr.astroware.chess.bot.evaluation;

import java.util.List;
import java.util.Objects;

/**
 * Évaluation globale d'une position depuis le point de vue du bot.
 *
 * <p>Convention pédagogique :</p>
 * <ul>
 *     <li>0 : position pratiquement perdue ;</li>
 *     <li>5 : position équilibrée ;</li>
 *     <li>10 : position très favorable.</li>
 * </ul>
 *
 * @param score note globale
 * @param criteria détail des critères
 * @param explanation explication synthétique
 */
public record PositionEvaluation(
    EvaluationScore score,
    List<EvaluationCriterion> criteria,
    String explanation
) {

    public PositionEvaluation {
        Objects.requireNonNull(score, "score must not be null");
        criteria = List.copyOf(
            Objects.requireNonNull(criteria, "criteria must not be null")
        );
        explanation = Objects.requireNonNullElse(explanation, "");
    }
}
