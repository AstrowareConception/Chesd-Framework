package fr.astroware.chess.bot.situation;

import fr.astroware.chess.bot.action.Actions;
import fr.astroware.chess.bot.api.BotContext;
import fr.astroware.chess.bot.evaluation.EvaluatedMove;
import fr.astroware.chess.bot.situation.detection.AttractionDetection;
import fr.astroware.chess.bot.situation.detection.BatteryDetection;
import fr.astroware.chess.bot.situation.detection.DeflectionDetection;
import fr.astroware.chess.bot.situation.detection.XRayDetection;
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

class AdvancedTacticSituationTest {

    @Test
    void detectsBatteryCreatedByQueenMove() {
        ChessRulesEngine engine =
            ChessRulesEngines.standard();

        PositionView position = engine.fromFen(
            "q7/7k/8/8/8/8/1Q5K/R7 w - - 0 1"
        );

        BotContext context =
            context(engine, position);

        Move batteryMove =
            Move.fromUci("b2a2");

        List<BatteryDetection> detections =
            Situations.batteryOpportunity()
                .detect(context);

        assertTrue(
            detections.stream()
                .anyMatch(detection ->
                    detection.move()
                        .equals(batteryMove)
                )
        );

        List<EvaluatedMove> candidates =
            Actions.playBestBattery()
                .evaluate(
                    context,
                    detections
                );

        assertTrue(
            candidates.stream()
                .anyMatch(candidate ->
                    candidate.move()
                        .equals(batteryMove)
                )
        );
    }

    @Test
    void detectsXRayCreatedByRookMove() {
        ChessRulesEngine engine =
            ChessRulesEngines.standard();

        PositionView position = engine.fromFen(
            "q7/7k/8/8/n7/8/7K/1R6 w - - 0 1"
        );

        BotContext context =
            context(engine, position);

        Move xRayMove =
            Move.fromUci("b1a1");

        List<XRayDetection> detections =
            Situations.xRayOpportunity()
                .detect(context);

        assertTrue(
            detections.stream()
                .anyMatch(detection ->
                    detection.move()
                        .equals(xRayMove)
                )
        );

        List<EvaluatedMove> candidates =
            Actions.playBestXRay()
                .evaluate(
                    context,
                    detections
                );

        assertTrue(
            candidates.stream()
                .anyMatch(candidate ->
                    candidate.move()
                        .equals(xRayMove)
                )
        );
    }

    @Test
    void detectsPressureOnUniqueDefender() {
        ChessRulesEngine engine =
            ChessRulesEngines.standard();

        PositionView position = engine.fromFen(
            "4r2k/8/5n2/8/8/6N1/8/7K w - - 0 1"
        );

        BotContext context =
            context(engine, position);

        Move deflectionMove =
            Move.fromUci("g3e4");

        List<DeflectionDetection> detections =
            Situations.deflectionOpportunity()
                .detect(context);

        assertTrue(
            detections.stream()
                .anyMatch(detection ->
                    detection.move()
                        .equals(deflectionMove)
                )
        );

        List<EvaluatedMove> candidates =
            Actions.playBestDeflection()
                .evaluate(
                    context,
                    detections
                );

        assertTrue(
            candidates.stream()
                .anyMatch(candidate ->
                    candidate.move()
                        .equals(deflectionMove)
                )
        );
    }

    @Test
    void detectsKingAttractionWithProfitableFollowUp() {
        ChessRulesEngine engine =
            ChessRulesEngines.standard();

        PositionView position = engine.fromFen(
            "q5k1/8/5P2/8/8/8/7K/R7 w - - 0 1"
        );

        BotContext context =
            context(engine, position);

        Move attractionMove =
            Move.fromUci("f6f7");

        List<AttractionDetection> detections =
            Situations.attractionOpportunity()
                .detect(context);

        AttractionDetection attraction =
            detections.stream()
                .filter(detection ->
                    detection.move()
                        .equals(attractionMove)
                )
                .findFirst()
                .orElseThrow();

        assertTrue(
            attraction.followUpCaptureValue()
                > attraction.sacrificedValue()
        );

        List<EvaluatedMove> candidates =
            Actions.playBestAttraction()
                .evaluate(
                    context,
                    detections
                );

        assertTrue(
            candidates.stream()
                .anyMatch(candidate ->
                    candidate.move()
                        .equals(attractionMove)
                )
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
