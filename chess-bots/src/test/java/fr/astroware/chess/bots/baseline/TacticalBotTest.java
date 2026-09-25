package fr.astroware.chess.bots.baseline;

import fr.astroware.chess.bot.api.BotContext;
import fr.astroware.chess.bot.api.BotDecision;
import fr.astroware.chess.core.game.GameStatus;
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

class TacticalBotTest {

    @Test
    void findsMateInOne() {
        ChessRulesEngine engine = ChessRulesEngines.standard();
        PositionView position = engine.fromFen(
            "7k/8/5KQ1/8/8/8/8/8 w - - 0 1"
        );

        BotContext context = context(engine, Color.WHITE, position);

        BotDecision decision = new TacticalBot().decide(context);
        PositionView after = engine.play(position, decision.move());

        assertEquals(
            GameStatus.WHITE_WINS,
            engine.result(after).status()
        );
        assertEquals(
            "Mater immédiatement",
            decision.trace().getFirst().ruleName()
        );
    }

    @Test
    void choosesHighValueKnightFork() {
        ChessRulesEngine engine = ChessRulesEngines.standard();
        PositionView position = engine.fromFen(
            "k7/3q1r2/8/8/2N5/8/8/K7 w - - 0 1"
        );

        BotContext context = context(engine, Color.WHITE, position);

        BotDecision decision = new TacticalBot().decide(context);

        assertEquals(Move.fromUci("c4e5"), decision.move());

        String selectedRule = decision.trace().stream()
            .filter(attempt -> attempt.selectedMove().isPresent())
            .findFirst()
            .orElseThrow()
            .ruleName();

        assertEquals("Créer une fourchette", selectedRule);
    }

    private static BotContext context(
        ChessRulesEngine engine,
        Color color,
        PositionView position
    ) {
        return new EngineContext(
            color,
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
