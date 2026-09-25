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

class GamePhaseTest {

    @Test
    void recognizesInitialPositionAsOpening() {
        ChessRulesEngine engine = ChessRulesEngines.standard();
        PositionView position = engine.initialPosition();

        assertEquals(
            GamePhase.OPENING,
            context(engine, position).analysis().gamePhase()
        );
    }

    @Test
    void recognizesReducedMaterialAsEndgame() {
        ChessRulesEngine engine = ChessRulesEngines.standard();
        PositionView position = engine.fromFen(
            "8/8/8/4k3/8/4K3/3P4/8 w - - 0 40"
        );

        assertEquals(
            GamePhase.ENDGAME,
            context(engine, position).analysis().gamePhase()
        );
    }

    @Test
    void recognizesLateFullMaterialPositionAsMiddlegame() {
        ChessRulesEngine engine = ChessRulesEngines.standard();
        PositionView position = engine.fromFen(
            "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 20"
        );

        assertEquals(
            GamePhase.MIDDLEGAME,
            context(engine, position).analysis().gamePhase()
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
