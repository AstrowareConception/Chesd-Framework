package fr.astroware.chess.bot.analysis;

import fr.astroware.chess.bot.api.BotContext;
import fr.astroware.chess.core.model.BoardFile;
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

class PositionEvaluationTest {

    @Test
    void detectsDoubledIsolatedPassedPawns() {
        ChessRulesEngine engine = ChessRulesEngines.standard();
        PositionView position = engine.fromFen(
            "7k/8/8/8/8/P7/P7/4K3 w - - 0 40"
        );

        PawnStructure structure =
            context(engine, position).analysis()
                .pawnStructure(Color.WHITE);

        assertEquals(2, structure.doubledCount());
        assertEquals(2, structure.isolatedCount());
        assertEquals(2, structure.passedCount());
    }

    @Test
    void detectsSemiOpenFile() {
        ChessRulesEngine engine = ChessRulesEngines.standard();
        PositionView position = engine.fromFen(
            "7k/8/8/8/8/8/3P4/4K3 w - - 0 40"
        );

        Analysis analysis = context(engine, position).analysis();

        assertEquals(
            FileStatus.BLACK_SEMI_OPEN,
            analysis.fileStatus(BoardFile.D)
        );
        assertEquals(
            FileStatus.OPEN,
            analysis.fileStatus(BoardFile.E)
        );
    }

    @Test
    void materialAdvantageRaisesPositionScore() {
        ChessRulesEngine engine = ChessRulesEngines.standard();
        PositionView position = engine.fromFen(
            "7k/8/8/8/8/8/8/Q3K3 w - - 0 40"
        );

        PositionEvaluation evaluation =
            context(engine, position).analysis()
                .positionEvaluation(Color.WHITE);

        assertTrue(evaluation.material() > 5.0);
        assertTrue(evaluation.total() > 5.0);
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
