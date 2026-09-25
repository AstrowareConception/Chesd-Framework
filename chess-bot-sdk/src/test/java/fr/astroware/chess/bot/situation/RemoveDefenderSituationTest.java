package fr.astroware.chess.bot.situation;

import fr.astroware.chess.bot.api.BotContext;
import fr.astroware.chess.bot.situation.detection.RemoveDefenderDetection;
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

class RemoveDefenderSituationTest {

    @Test
    void detectsCaptureThatExposesBothTargets() {
        ChessRulesEngine engine = ChessRulesEngines.standard();
        PositionView position = engine.fromFen(
            "7k/4b3/3n1n2/8/8/8/8/3RRR1K w - - 0 1"
        );

        BotContext context = new EngineContext(
            Color.WHITE,
            position,
            engine.legalMoves(position)
        );

        List<RemoveDefenderDetection> detections =
            Situations.removeOverloadedDefenderOpportunity()
                .detect(context);

        assertTrue(
            detections.stream()
                .anyMatch(detection ->
                    detection.move().equals(Move.fromUci("e1e7"))
                        && detection.newlyHangingTargets().size() == 2
                        && detection.exposedValue() == 6
                )
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
