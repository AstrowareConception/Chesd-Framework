package fr.astroware.chess.bot.search;

import fr.astroware.chess.bot.analysis.PositionEvaluation;
import fr.astroware.chess.core.model.Move;

import java.util.List;
import java.util.Objects;

/**
 * Résultat d'une recherche pour un coup racine.
 *
 * @param move coup racine
 * @param score score Minimax du point de vue du bot
 * @param immediateEvaluation évaluation immédiatement après le coup racine
 * @param principalVariation ligne principale trouvée
 * @param nodesVisited nombre de nœuds visités
 * @param cutoffs nombre de coupures alpha-bêta
 * @param depth profondeur demandée
 */
public record SearchEvaluation(
    Move move,
    double score,
    PositionEvaluation immediateEvaluation,
    List<Move> principalVariation,
    int nodesVisited,
    int cutoffs,
    int depth
) {

    public SearchEvaluation {
        Objects.requireNonNull(move, "move must not be null");
        Objects.requireNonNull(
            immediateEvaluation,
            "immediateEvaluation must not be null"
        );
        principalVariation = List.copyOf(
            Objects.requireNonNull(
                principalVariation,
                "principalVariation must not be null"
            )
        );

        if (score < 0.0 || score > 10.0) {
            throw new IllegalArgumentException(
                "score must be between 0 and 10"
            );
        }

        if (nodesVisited < 0 || cutoffs < 0) {
            throw new IllegalArgumentException(
                "search counters must be non-negative"
            );
        }

        if (depth <= 0) {
            throw new IllegalArgumentException(
                "depth must be > 0"
            );
        }
    }

    public String principalVariationUci() {
        return principalVariation.stream()
            .map(Move::toUci)
            .collect(
                java.util.stream.Collectors.joining(" ")
            );
    }
}
