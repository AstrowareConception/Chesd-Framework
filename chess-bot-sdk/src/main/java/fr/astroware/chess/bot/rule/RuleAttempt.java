package fr.astroware.chess.bot.rule;

import fr.astroware.chess.bot.evaluation.EvaluatedMove;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Trace détaillée de l'évaluation d'une règle.
 *
 * <p>La trace conserve l'ensemble des coups candidats évalués, et pas
 * seulement le coup finalement choisi. Elle permet donc de comprendre
 * pourquoi une règle a préféré un coup à un autre.</p>
 *
 * @param ruleName nom lisible de la règle
 * @param status résultat de l'évaluation
 * @param detectionCount nombre d'occurrences détectées
 * @param candidates tous les coups candidats proposés par l'action
 * @param selectedMove meilleur candidat légal retenu
 * @param explanation information utile au débogage
 */
public record RuleAttempt(
    String ruleName,
    AttemptStatus status,
    int detectionCount,
    List<EvaluatedMove> candidates,
    Optional<EvaluatedMove> selectedMove,
    String explanation
) {

    public RuleAttempt {
        Objects.requireNonNull(ruleName, "ruleName must not be null");
        Objects.requireNonNull(status, "status must not be null");
        candidates = List.copyOf(
            Objects.requireNonNull(candidates, "candidates must not be null")
        );
        Objects.requireNonNull(selectedMove, "selectedMove must not be null");
        explanation = Objects.requireNonNullElse(explanation, "");

        if (detectionCount < 0) {
            throw new IllegalArgumentException("detectionCount must be >= 0");
        }
    }
}
