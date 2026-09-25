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

class PressureBotTest {

    @Test
    void removesOverloadedDefender() {
        ChessRulesEngine engine = ChessRulesEngines.standard();
        PositionView position = engine.fromFen(
            "7k/4b3/3n1n2/8/8/8/8/3RRR1K w - - 0 1"
        );

        BotDecision decision = new PressureBot().decide(
            new EngineContext(
                Color.WHITE,
                position,
                engine.legalMoves(position)
            )
        );

        assertEquals(Move.fromUci("e1e7"), decision.move());

        String selectedRule = decision.trace().stream()
            .filter(attempt -> attempt.selectedMove().isPresent())
            .findFirst()
            .orElseThrow()
            .ruleName();

        assertEquals(
            "Éliminer un défenseur surchargé",
            selectedRule
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
