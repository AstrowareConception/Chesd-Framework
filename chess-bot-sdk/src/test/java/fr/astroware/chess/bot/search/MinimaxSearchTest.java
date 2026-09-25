package fr.astroware.chess.bot.search;

import fr.astroware.chess.bot.api.BotContext;
import fr.astroware.chess.core.model.Color;
import fr.astroware.chess.core.model.Move;
import fr.astroware.chess.core.model.PositionView;
import fr.astroware.chess.core.rules.ChessRulesEngine;
import fr.astroware.chess.core.rules.ChessRulesEngines;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.random.RandomGenerator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MinimaxSearchTest {

    @Test
    void depthTwoPenalizesRefutedQueenCapture() {
        ChessRulesEngine engine = ChessRulesEngines.standard();
        PositionView position = engine.fromFen(
            "r7/p6k/8/8/8/8/8/Q6K w - - 0 1"
        );

        BotContext context = context(engine, position);

        SearchEvaluation queenCapture =
            MinimaxSearch.evaluate(
                context,
                Color.WHITE,
                SearchSettings.exact(2)
            ).stream()
                .filter(evaluation ->
                    evaluation.move().equals(
                        Move.fromUci("a1a7")
                    )
                )
                .findFirst()
                .orElseThrow();

        assertTrue(queenCapture.score() < 5.0);
        assertTrue(
            queenCapture.principalVariation().size() >= 2
        );
        assertEquals(
            Move.fromUci("a8a7"),
            queenCapture.principalVariation().get(1)
        );
    }

    @Test
    void alphaBetaKeepsSameBestScore() {
        ChessRulesEngine engine = ChessRulesEngines.standard();
        PositionView position = engine.fromFen(
            "8/8/8/4k3/8/4K3/3P4/8 w - - 0 40"
        );

        BotContext context = context(engine, position);

        SearchSettings withPruning =
            SearchSettings.bounded(3, 5);

        SearchSettings withoutPruning =
            withPruning.withoutAlphaBeta();

        SearchEvaluation bestWithPruning =
            best(
                MinimaxSearch.evaluate(
                    context,
                    Color.WHITE,
                    withPruning
                )
            );

        SearchEvaluation bestWithoutPruning =
            best(
                MinimaxSearch.evaluate(
                    context,
                    Color.WHITE,
                    withoutPruning
                )
            );

        assertEquals(
            bestWithoutPruning.score(),
            bestWithPruning.score()
        );
        assertFalse(
            bestWithPruning.principalVariation().isEmpty()
        );
    }

    private static SearchEvaluation best(
        List<SearchEvaluation> evaluations
    ) {
        return evaluations.stream()
            .max(
                Comparator.comparingDouble(
                    SearchEvaluation::score
                )
            )
            .orElseThrow();
    }

    private static BotContext context(
        ChessRulesEngine engine,
        PositionView position
    ) {
        return new EngineContext(
            position.sideToMove(),
            position,
            engine.legalMoves(position)
        );
    }

    private record EngineContext(
        Color myColor,
        PositionView position,
        List<Move> legalMoves
    ) implements BotContext {

        @Override
        public RandomGenerator random() {
            return new Random(42L);
        }
    }
}
