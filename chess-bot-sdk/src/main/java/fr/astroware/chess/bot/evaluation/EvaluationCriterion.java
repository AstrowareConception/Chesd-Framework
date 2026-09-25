package fr.astroware.chess.bot.evaluation;

import java.util.Objects;

/**
 * Un élément explicatif d'une évaluation.
 *
 * <p>Exemple : matériel = 8/10, sécurité du roi = 4/10, mobilité = 7/10.</p>
 *
 * @param name nom du critère
 * @param score note du critère
 * @param weight poids indicatif utilisé dans l'évaluation globale
 * @param explanation explication destinée à la trace et au débogage
 */
public record EvaluationCriterion(
    String name,
    EvaluationScore score,
    double weight,
    String explanation
) {

    public EvaluationCriterion {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(score, "score must not be null");
        explanation = Objects.requireNonNullElse(explanation, "");

        if (name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }

        if (!Double.isFinite(weight) || weight < 0.0) {
            throw new IllegalArgumentException(
                "weight must be a finite positive or null value"
            );
        }
    }
}
