package fr.astroware.chess.bot.situation.detection;

import fr.astroware.chess.bot.rule.Detection;
import fr.astroware.chess.core.model.Move;

import java.util.Objects;

/**
 * Coup améliorant le contrôle du centre.
 *
 * @param move coup envisagé
 * @param beforeScore score de centre avant le coup
 * @param afterScore score de centre après le coup
 */
public record CenterImprovementDetection(
    Move move,
    double beforeScore,
    double afterScore
) implements Detection {

    public CenterImprovementDetection {
        Objects.requireNonNull(
            move,
            "move must not be null"
        );

        validate(beforeScore, "beforeScore");
        validate(afterScore, "afterScore");

        if (afterScore <= beforeScore) {
            throw new IllegalArgumentException(
                "afterScore must improve center control"
            );
        }
    }

    public double gain() {
        return afterScore - beforeScore;
    }

    private static void validate(
        double value,
        String name
    ) {
        if (!Double.isFinite(value)
            || value < 0.0
            || value > 10.0) {
            throw new IllegalArgumentException(
                name + " must be between 0 and 10"
            );
        }
    }
}
