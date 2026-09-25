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
import static org.junit.jupiter.api.Assertions.assertTrue;

class PositionalBotTest {

    @Test
    void usesGlobalEvaluationWhenNoUrgencyExists() {
        ChessRulesEngine engine = ChessRulesEngines.standard();
        PositionView position = engine.fromFen(
            "8/8/8/4k3/8/4K3/3P4/8 w - - 0 40"
        );

        BotContext context = new EngineContext(
            Color.WHITE,
            position,
            engine.legalMoves(position)
        );

        BotDecision decision = new PositionalBot().decide(context);

        assertTrue(engine.legalMoves(position).contains(decision.move()));

        String selectedRule = decision.trace().stream()
            .filter(attempt -> attempt.selectedMove().isPresent())
            .findFirst()
            .orElseThrow()
            .ruleName();

        assertEquals("Évaluer la position", selectedRule);
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
