package fr.astroware.chess.bots.baseline;

import fr.astroware.chess.bot.api.BotContext;
import fr.astroware.chess.bot.api.BotDecision;
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
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class MinimaxBotTest {

    @Test
    void rejectsImmediateQueenBlunder() {
        ChessRulesEngine engine = ChessRulesEngines.standard();
        PositionView position = engine.fromFen(
            "r7/p6k/8/8/8/8/8/Q6K w - - 0 1"
        );

        BotDecision decision = new MinimaxBot().decide(
            new EngineContext(
                Color.WHITE,
                position,
                engine.legalMoves(position)
            )
        );

        assertNotEquals(
            Move.fromUci("a1a7"),
            decision.move()
        );

        String selectedRule = decision.trace().stream()
            .filter(attempt -> attempt.selectedMove().isPresent())
            .findFirst()
            .orElseThrow()
            .ruleName();

        assertEquals("Recherche Minimax", selectedRule);
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
