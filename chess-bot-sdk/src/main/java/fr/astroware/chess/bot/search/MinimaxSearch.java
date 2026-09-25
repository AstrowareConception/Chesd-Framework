package fr.astroware.chess.bot.search;

import fr.astroware.chess.bot.analysis.PositionEvaluation;
import fr.astroware.chess.bot.analysis.PositionProjection;
import fr.astroware.chess.bot.api.BotContext;
import fr.astroware.chess.core.game.GameStatus;
import fr.astroware.chess.core.model.Color;
import fr.astroware.chess.core.model.Move;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Recherche Minimax pédagogique avec élagage alpha-bêta facultatif.
 *
 * <p>Le classement des coups avant exploration utilise l'évaluation
 * positionnelle du framework. Ce move ordering rend l'alpha-bêta plus
 * efficace et garde la mécanique explicable.</p>
 */
public final class MinimaxSearch {

    private MinimaxSearch() {
    }

    public static List<SearchEvaluation> evaluate(
        BotContext context,
        Color perspective,
        SearchSettings settings
    ) {
        Objects.requireNonNull(context, "context must not be null");
        Objects.requireNonNull(
            perspective,
            "perspective must not be null"
        );
        Objects.requireNonNull(
            settings,
            "settings must not be null"
        );

        List<RootCandidate> roots =
            orderedRootCandidates(
                context,
                perspective,
                settings.maxMovesPerNode()
            );

        List<SearchEvaluation> evaluations =
            new ArrayList<>();

        for (RootCandidate root : roots) {
            SearchStats stats = new SearchStats();
            stats.nodesVisited++;

            NodeResult result;

            if (settings.depth() == 1
                || root.projection().result().isOver()) {

                result = terminalOrStatic(
                    root.projection(),
                    perspective
                );
            } else {
                result = minimax(
                    root.projection(),
                    perspective,
                    settings.depth() - 1,
                    settings,
                    0.0,
                    10.0,
                    stats
                );
            }

            List<Move> principalVariation =
                new ArrayList<>();
            principalVariation.add(root.move());
            principalVariation.addAll(
                result.principalVariation()
            );

            evaluations.add(
                new SearchEvaluation(
                    root.move(),
                    result.score(),
                    root.immediateEvaluation(),
                    principalVariation,
                    stats.nodesVisited,
                    stats.cutoffs,
                    settings.depth()
                )
            );
        }

        return List.copyOf(evaluations);
    }

    private static NodeResult minimax(
        PositionProjection node,
        Color perspective,
        int remainingDepth,
        SearchSettings settings,
        double alpha,
        double beta,
        SearchStats stats
    ) {
        stats.nodesVisited++;

        if (node.result().isOver()
            || remainingDepth == 0
            || node.legalMoves().isEmpty()) {

            return terminalOrStatic(
                node,
                perspective
            );
        }

        boolean maximizing =
            node.position().sideToMove() == perspective;

        List<ChildCandidate> children =
            orderedChildren(
                node,
                perspective,
                maximizing,
                settings.maxMovesPerNode()
            );

        if (children.isEmpty()) {
            return terminalOrStatic(
                node,
                perspective
            );
        }

        double bestScore =
            maximizing
                ? Double.NEGATIVE_INFINITY
                : Double.POSITIVE_INFINITY;

        List<Move> bestVariation = List.of();

        double localAlpha = alpha;
        double localBeta = beta;

        for (ChildCandidate child : children) {
            NodeResult childResult = minimax(
                child.projection(),
                perspective,
                remainingDepth - 1,
                settings,
                localAlpha,
                localBeta,
                stats
            );

            boolean better = maximizing
                ? childResult.score() > bestScore
                : childResult.score() < bestScore;

            if (better) {
                bestScore = childResult.score();

                List<Move> variation =
                    new ArrayList<>();
                variation.add(child.move());
                variation.addAll(
                    childResult.principalVariation()
                );
                bestVariation = List.copyOf(variation);
            }

            if (settings.alphaBeta()) {
                if (maximizing) {
                    localAlpha = Math.max(
                        localAlpha,
                        bestScore
                    );
                } else {
                    localBeta = Math.min(
                        localBeta,
                        bestScore
                    );
                }

                if (localBeta <= localAlpha) {
                    stats.cutoffs++;
                    break;
                }
            }
        }

        return new NodeResult(
            Math.clamp(bestScore, 0.0, 10.0),
            bestVariation
        );
    }

    private static List<RootCandidate>
        orderedRootCandidates(
            BotContext context,
            Color perspective,
            int limit
        ) {

        return context.legalMoves().stream()
            .map(move -> {
                PositionProjection projection =
                    context.analysis().after(move);

                PositionEvaluation evaluation =
                    projection.analysis()
                        .positionEvaluation(perspective);

                return new RootCandidate(
                    move,
                    projection,
                    evaluation
                );
            })
            .sorted(
                Comparator.comparingDouble(
                    (RootCandidate candidate) ->
                        terminalAwareScore(
                            candidate.projection(),
                            candidate.immediateEvaluation(),
                            perspective
                        )
                ).reversed()
            )
            .limit(limit)
            .toList();
    }

    private static List<ChildCandidate>
        orderedChildren(
            PositionProjection node,
            Color perspective,
            boolean maximizing,
            int limit
        ) {

        Comparator<ChildCandidate> comparator =
            Comparator.comparingDouble(
                candidate ->
                    terminalAwareScore(
                        candidate.projection(),
                        candidate.evaluation(),
                        perspective
                    )
            );

        if (maximizing) {
            comparator = comparator.reversed();
        }

        return node.legalMoves().stream()
            .map(move -> {
                PositionProjection projection =
                    node.analysis().after(move);

                PositionEvaluation evaluation =
                    projection.analysis()
                        .positionEvaluation(perspective);

                return new ChildCandidate(
                    move,
                    projection,
                    evaluation
                );
            })
            .sorted(comparator)
            .limit(limit)
            .toList();
    }

    private static NodeResult terminalOrStatic(
        PositionProjection node,
        Color perspective
    ) {
        if (node.result().isOver()) {
            return new NodeResult(
                terminalScore(
                    node.result().status(),
                    perspective
                ),
                List.of()
            );
        }

        return new NodeResult(
            node.analysis()
                .positionEvaluation(perspective)
                .total(),
            List.of()
        );
    }

    private static double terminalAwareScore(
        PositionProjection projection,
        PositionEvaluation evaluation,
        Color perspective
    ) {
        return projection.result().isOver()
            ? terminalScore(
                projection.result().status(),
                perspective
            )
            : evaluation.total();
    }

    private static double terminalScore(
        GameStatus status,
        Color perspective
    ) {
        return switch (status) {
            case WHITE_WINS ->
                perspective == Color.WHITE
                    ? 10.0
                    : 0.0;
            case BLACK_WINS ->
                perspective == Color.BLACK
                    ? 10.0
                    : 0.0;
            case DRAW -> 5.0;
            case ONGOING -> 5.0;
        };
    }

    private record RootCandidate(
        Move move,
        PositionProjection projection,
        PositionEvaluation immediateEvaluation
    ) {
    }

    private record ChildCandidate(
        Move move,
        PositionProjection projection,
        PositionEvaluation evaluation
    ) {
    }

    private record NodeResult(
        double score,
        List<Move> principalVariation
    ) {
    }

    private static final class SearchStats {
        private int nodesVisited;
        private int cutoffs;
    }
}
