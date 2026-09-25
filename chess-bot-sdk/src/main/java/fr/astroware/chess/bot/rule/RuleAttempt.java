package fr.astroware.chess.bot.rule;

import fr.astroware.chess.core.model.Move;

import java.util.Objects;
import java.util.Optional;

/**
 * Trace de l'évaluation d'une règle.
 *
 * @param ruleName nom lisible de la règle
 * @param status résultat de l'évaluation
 * @param detectionCount nombre d'occurrences détectées
 * @param proposedMove coup proposé, s'il y en a un
 * @param explanation information utile au débogage
 */
public record RuleAttempt(
    String ruleName,
    AttemptStatus status,
    int detectionCount,
    Optional<Move> proposedMove,
    String explanation
) {

    public RuleAttempt {
        Objects.requireNonNull(ruleName, "ruleName must not be null");
        Objects.requireNonNull(status, "status must not be null");
        Objects.requireNonNull(proposedMove, "proposedMove must not be null");
        explanation = Objects.requireNonNullElse(explanation, "");

        if (detectionCount < 0) {
            throw new IllegalArgumentException("detectionCount must be >= 0");
        }
    }
}
