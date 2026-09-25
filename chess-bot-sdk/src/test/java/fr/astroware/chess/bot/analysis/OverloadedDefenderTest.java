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

class OverloadedDefenderTest {

    @Test
    void detectsSoleDefenderOfTwoAttackedPieces() {
        ChessRulesEngine engine = ChessRulesEngines.standard();
        PositionView position = engine.fromFen(
            "7k/4b3/3n1n2/8/8/8/8/3RRR1K w - - 0 1"
        );

        BotContext context = new EngineContext(
            Color.WHITE,
            position,
            engine.legalMoves(position)
        );

        List<OverloadedDefenderPattern> overloads =
            context.analysis().overloadedDefenders(Color.BLACK);

        assertEquals(1, overloads.size());
        assertEquals(
            "e7",
            overloads.getFirst().defender().square().notation()
        );
        assertEquals(
            2,
            overloads.getFirst().protectedTargets().size()
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
