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

class LineTacticAnalysisTest {

    @Test
    void detectsAbsolutePin() {
        ChessRulesEngine engine = ChessRulesEngines.standard();
        PositionView position = engine.fromFen(
            "4k3/4n3/8/8/8/8/8/4R2K b - - 0 1"
        );

        Analysis analysis = context(engine, position, Color.BLACK).analysis();

        List<PinPattern> pins = analysis.pinsBy(Color.WHITE);

        assertEquals(1, pins.size());
        assertEquals("e1", pins.getFirst().attacker().square().notation());
        assertEquals("e7", pins.getFirst().pinned().square().notation());
        assertEquals("e8", pins.getFirst().king().square().notation());
    }

    @Test
    void detectsSkewer() {
        ChessRulesEngine engine = ChessRulesEngines.standard();
        PositionView position = engine.fromFen(
            "4r2k/4q3/8/8/8/8/8/4R2K b - - 0 1"
        );

        Analysis analysis = context(engine, position, Color.BLACK).analysis();

        List<SkewerPattern> skewers = analysis.skewersBy(Color.WHITE);

        assertEquals(1, skewers.size());
        assertEquals("e7", skewers.getFirst().front().square().notation());
        assertEquals("e8", skewers.getFirst().rear().square().notation());
    }

    private static BotContext context(
        ChessRulesEngine engine,
        PositionView position,
        Color color
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
