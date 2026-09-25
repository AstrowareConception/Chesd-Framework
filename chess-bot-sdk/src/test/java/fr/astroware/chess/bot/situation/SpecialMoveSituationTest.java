package fr.astroware.chess.bot.situation;

import fr.astroware.chess.bot.action.Actions;
import fr.astroware.chess.bot.api.BotContext;
import fr.astroware.chess.bot.evaluation.EvaluatedMove;
import fr.astroware.chess.bot.situation.detection.CastlingDetection;
import fr.astroware.chess.bot.situation.detection.PromotionDetection;
import fr.astroware.chess.core.model.Color;
import fr.astroware.chess.core.model.Move;
import fr.astroware.chess.core.model.PositionView;
import fr.astroware.chess.core.rules.ChessRulesEngine;
import fr.astroware.chess.core.rules.ChessRulesEngines;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.random.RandomGenerator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpecialMoveSituationTest {

    @Test
    void detectsFourPromotionChoicesAndPrefersQueen() {
        ChessRulesEngine engine =
            ChessRulesEngines.standard();

        PositionView position = engine.fromFen(
            "4k3/P7/8/8/8/8/8/4K3 w - - 0 1"
        );

        BotContext context =
            context(engine, position);

        List<PromotionDetection> detections =
            Situations.promotionAvailable()
                .detect(context);

        assertEquals(4, detections.size());

        List<EvaluatedMove> candidates =
            Actions.playBestPromotion()
                .evaluate(
                    context,
                    detections
                );

        EvaluatedMove best =
            candidates.stream()
                .max(
                    Comparator.comparingDouble(
                        candidate ->
                            candidate.score().value()
                    )
                )
                .orElseThrow();

        assertEquals(
            Move.fromUci("a7a8q"),
            best.move()
        );
    }

    @Test
    void detectsBothLegalCastles() {
        ChessRulesEngine engine =
            ChessRulesEngines.standard();

        PositionView position = engine.fromFen(
            "r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1"
        );

        BotContext context =
            context(engine, position);

        List<CastlingDetection> detections =
            Situations.castlingAvailable()
                .detect(context);

        assertEquals(2, detections.size());

        assertTrue(
            detections.stream()
                .anyMatch(
                    detection ->
                        detection.kingSide()
                            && detection.move().equals(
                                Move.fromUci("e1g1")
                            )
                )
        );

        assertTrue(
            detections.stream()
                .anyMatch(
                    detection ->
                        !detection.kingSide()
                            && detection.move().equals(
                                Move.fromUci("e1c1")
                            )
                )
        );

        List<EvaluatedMove> candidates =
            Actions.playBestCastle()
                .evaluate(
                    context,
                    detections
                );

        assertEquals(2, candidates.size());

        candidates.forEach(candidate ->
            assertTrue(
                candidate.score().value()
                    >= 0.0
                    && candidate.score().value()
                    <= 10.0
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
