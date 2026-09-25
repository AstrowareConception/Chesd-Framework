package fr.astroware.chess.bot.plan;

import fr.astroware.chess.bot.api.BotContext;
import fr.astroware.chess.bot.evaluation.EvaluatedMove;
import fr.astroware.chess.core.model.Color;
import fr.astroware.chess.core.model.Move;
import fr.astroware.chess.core.model.PositionView;
import fr.astroware.chess.core.rules.ChessRulesEngine;
import fr.astroware.chess.core.rules.ChessRulesEngines;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;
import java.util.random.RandomGenerator;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PositionalPlansTest {

    @Test
    void createPassedPawnPlanFindsCaptureCreatingPassedPawn() {
        ChessRulesEngine engine = ChessRulesEngines.standard();
        PositionView position = engine.fromFen(
            "7k/8/8/4p3/3P4/8/8/7K w - - 0 40"
        );

        BotContext context = context(engine, position);

        List<EvaluatedMove> candidates =
            Plans.createPassedPawn().candidates(context);

        assertTrue(
            candidates.stream()
                .anyMatch(candidate ->
                    candidate.move().equals(
                        Move.fromUci("d4e5")
                    )
                )
        );
    }

    @Test
    void openFilePlanFindsRookActivation() {
        ChessRulesEngine engine = ChessRulesEngines.standard();
        PositionView position = engine.fromFen(
            "7k/8/8/8/8/8/8/R3K3 w - - 0 20"
        );

        BotContext context = context(engine, position);

        List<EvaluatedMove> candidates =
            Plans.useOpenFile().candidates(context);

        assertTrue(
            candidates.stream()
                .anyMatch(candidate ->
                    candidate.move().equals(
                        Move.fromUci("a1d1")
                    )
                )
        );
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
