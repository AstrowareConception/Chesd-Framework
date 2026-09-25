package fr.astroware.chess.bot.analysis;

import fr.astroware.chess.bot.api.BotContext;
import fr.astroware.chess.core.model.Color;
import fr.astroware.chess.core.model.Move;
import fr.astroware.chess.core.model.PositionView;
import fr.astroware.chess.core.rules.ChessRulesEngine;
import fr.astroware.chess.core.rules.ChessRulesEngines;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;
import java.util.random.RandomGenerator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdversarialEvaluationTest {

    @Test
    void penalizesApparentlyProfitableCaptureThatLosesQueen() {
        ChessRulesEngine engine = ChessRulesEngines.standard();
        PositionView position = engine.fromFen(
            "r7/p6k/8/8/8/8/8/Q6K w - - 0 1"
        );

        BotContext context = context(engine, position);

        AdversarialEvaluation evaluation =
            context.analysis().adversarialEvaluation(
                Move.fromUci("a1a7"),
                Color.WHITE
            );

        assertEquals(
            Move.fromUci("a8a7"),
            evaluation.bestReply().orElseThrow()
        );

        assertTrue(
            evaluation.immediateEvaluation().total()
                > evaluation.robustScore()
        );

        assertTrue(evaluation.robustScore() < 5.0);
    }

    @Test
    void mateInOneKeepsMaximumRobustScore() {
        ChessRulesEngine engine = ChessRulesEngines.standard();
        PositionView position = engine.fromFen(
            "7k/8/5KQ1/8/8/8/8/8 w - - 0 1"
        );

        BotContext context = context(engine, position);
        Move mate = context.analysis()
            .mateInOneMoves()
            .getFirst();

        AdversarialEvaluation evaluation =
            context.analysis().adversarialEvaluation(
                mate,
                Color.WHITE
            );

        assertEquals(10.0, evaluation.robustScore());
        assertTrue(evaluation.bestReply().isEmpty());
        assertEquals(0, evaluation.replyCount());
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
