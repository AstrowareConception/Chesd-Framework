package fr.astroware.chess.bot.situation;

import fr.astroware.chess.bot.api.BotContext;
import fr.astroware.chess.bot.rule.PresenceDetection;
import fr.astroware.chess.bot.situation.detection.CheckingMoveDetection;
import fr.astroware.chess.bot.situation.detection.PinDetection;
import fr.astroware.chess.bot.situation.detection.SkewerDetection;
import fr.astroware.chess.core.model.Color;
import fr.astroware.chess.core.model.Move;
import fr.astroware.chess.core.model.PositionView;
import fr.astroware.chess.core.rules.ChessRulesEngine;
import fr.astroware.chess.core.rules.ChessRulesEngines;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;
import java.util.random.RandomGenerator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdvancedSituationsTest {

    @Test
    void recognizesCheck() {
        ChessRulesEngine engine = ChessRulesEngines.standard();
        PositionView position = engine.fromFen(
            "4r2k/8/8/8/8/8/8/4K3 w - - 0 1"
        );

        List<PresenceDetection> detections =
            Situations.inCheck().detect(context(engine, position, Color.WHITE));

        assertFalse(detections.isEmpty());
    }

    @Test
    void detectsCheckingMoves() {
        ChessRulesEngine engine = ChessRulesEngines.standard();
        PositionView position = engine.fromFen(
            "7k/8/8/8/8/8/4R3/4K3 w - - 0 1"
        );

        List<CheckingMoveDetection> detections =
            Situations.checkAvailable()
                .detect(context(engine, position, Color.WHITE));

        assertFalse(detections.isEmpty());
        assertTrue(
            detections.stream()
                .anyMatch(detection ->
                    detection.move().equals(Move.fromUci("e2e8"))
                )
        );
    }

    @Test
    void detectsMoveCreatingPin() {
        ChessRulesEngine engine = ChessRulesEngines.standard();
        PositionView position = engine.fromFen(
            "4k3/4n3/8/8/8/8/8/R6K w - - 0 1"
        );

        List<PinDetection> detections =
            Situations.pinOpportunity()
                .detect(context(engine, position, Color.WHITE));

        assertTrue(
            detections.stream()
                .anyMatch(detection ->
                    detection.move().equals(Move.fromUci("a1e1"))
                )
        );
    }

    @Test
    void detectsMoveCreatingSkewer() {
        ChessRulesEngine engine = ChessRulesEngines.standard();
        PositionView position = engine.fromFen(
            "4r2k/4q3/8/8/8/8/8/R6K w - - 0 1"
        );

        List<SkewerDetection> detections =
            Situations.skewerOpportunity()
                .detect(context(engine, position, Color.WHITE));

        assertTrue(
            detections.stream()
                .anyMatch(detection ->
                    detection.move().equals(Move.fromUci("a1e1"))
                )
        );
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
