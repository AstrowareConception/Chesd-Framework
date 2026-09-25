package fr.astroware.chess.bot.evaluation;

import fr.astroware.chess.core.model.Move;

import java.util.List;
import java.util.Objects;

/**
 * Un coup candidat accompagné de son évaluation.
 *
 * <p>Une action peut produire plusieurs instances. La règle peut alors
 * comparer les candidats et retenir celui qui possède la meilleure note.</p>
 *
 * @param move coup candidat
 * @param score qualité globale estimée du coup, de 0 à 10
 * @param criteria détail des critères ayant contribué à l'évaluation
 * @param explanation explication synthétique
 */
public record EvaluatedMove(
    Move move,
    EvaluationScore score,
    List<EvaluationCriterion> criteria,
    String explanation
) {

    public EvaluatedMove {
        Objects.requireNonNull(move, "move must not be null");
        Objects.requireNonNull(score, "score must not be null");
        criteria = List.copyOf(
            Objects.requireNonNull(criteria, "criteria must not be null")
        );
        explanation = Objects.requireNonNullElse(explanation, "");
    }

    public static EvaluatedMove of(
        Move move,
        double score,
        String explanation
    ) {
        return new EvaluatedMove(
            move,
            EvaluationScore.of(score),
            List.of(),
            explanation
        );
    }
}
