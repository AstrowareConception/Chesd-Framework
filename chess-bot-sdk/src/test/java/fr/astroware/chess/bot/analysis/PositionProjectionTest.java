package fr.astroware.chess.bot.analysis;

import fr.astroware.chess.bot.api.BotContext;
import fr.astroware.chess.core.model.Color;
import fr.astroware.chess.core.model.Move;
import fr.astroware.chess.core.model.PositionView;
import fr.astroware.chess.core.model.Square;
import fr.astroware.chess.core.rules.ChessRulesEngine;
import fr.astroware.chess.core.rules.ChessRulesEngines;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;
import java.util.random.RandomGenerator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PositionProjectionTest {

    @Test
    void projectsMoveWithoutChangingSourcePosition() {
        ChessRulesEngine engine = ChessRulesEngines.standard();
        PositionView initial = engine.initialPosition();

        BotContext context = new EngineContext(
            Color.WHITE,
            initial,
            engine.legalMoves(initial),
            List.of()
        );

        PositionProjection projection =
            context.analysis().after(Move.fromUci("e2e4"));

        assertTrue(initial.pieceAt(Square.from("e2")).isPresent());
        assertTrue(projection.position().pieceAt(Square.from("e2")).isEmpty());
        assertTrue(projection.position().pieceAt(Square.from("e4")).isPresent());
        assertEquals(Color.BLACK, projection.position().sideToMove());
        assertEquals(20, projection.legalMoves().size());
    }

    private record EngineContext(
        Color myColor,
        PositionView position,
        List<Move> legalMoves,
        List<Move> moveHistory
    ) implements BotContext {

        @Override
        public RandomGenerator random() {
            return new Random(42L);
        }
    }
}
