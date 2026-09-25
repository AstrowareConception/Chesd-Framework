package fr.astroware.chess.bot.analysis;

import fr.astroware.chess.core.model.Color;

import java.util.Objects;

/**
 * Évaluation pédagogique d'une position, normalisée de 0 à 10.
 *
 * <p>5 représente une position globalement équilibrée du point de vue du camp
 * analysé. Les composantes sont également exprimées sur 10 afin de rester
 * faciles à expliquer aux étudiants.</p>
 */
public record PositionEvaluation(
    Color perspective,
    double total,
    double material,
    double mobility,
    double centerControl,
    double pawnStructure,
    double kingSafety,
    String explanation
) {

    public PositionEvaluation {
        Objects.requireNonNull(perspective, "perspective must not be null");
        Objects.requireNonNull(explanation, "explanation must not be null");

        validate(total, "total");
        validate(material, "material");
        validate(mobility, "mobility");
        validate(centerControl, "centerControl");
        validate(pawnStructure, "pawnStructure");
        validate(kingSafety, "kingSafety");
    }

    private static void validate(double value, String name) {
        if (value < 0.0 || value > 10.0) {
            throw new IllegalArgumentException(
                name + " must be between 0 and 10"
            );
        }
    }
}
