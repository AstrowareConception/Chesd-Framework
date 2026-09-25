package fr.astroware.chess.bot.situation;

import fr.astroware.chess.bot.action.Actions;
import fr.astroware.chess.bot.api.BotContext;
import fr.astroware.chess.bot.evaluation.EvaluatedMove;
import fr.astroware.chess.bot.situation.detection.CenterImprovementDetection;
import fr.astroware.chess.bot.situation.detection.DevelopmentDetection;
import fr.astroware.chess.bot.situation.detection.ThreatenedPieceDetection;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PositionalSituationTest {

    @Test
    void detectsAttackedAndUnderDefendedOwnPiece() {
        ChessRulesEngine engine =
            ChessRulesEngines.standard();

        PositionView position = engine.fromFen(
            "4r1k1/8/8/1b6/8/8/4Q3/4R1K1 w - - 0 1"
        );

        BotContext context =
            context(engine, position);

        List<ThreatenedPieceDetection> attacked =
            Situations.attackedOwnPiece()
                .detect(context);

        ThreatenedPieceDetection queen =
            attacked.stream()
                .filter(detection ->
                    detection.piece()
                        .square()
                        .equals(
                            Square.from("e2")
                        )
                )
                .findFirst()
                .orElseThrow();

        assertEquals(2, queen.attackers());
        assertEquals(1, queen.defenders());

        List<ThreatenedPieceDetection> underDefended =
            Situations.underDefendedOwnPiece()
                .detect(context);

        assertTrue(
            underDefended.stream()
                .anyMatch(detection ->
                    detection.piece()
                        .square()
                        .equals(
                            Square.from("e2")
                        )
                )
        );

        List<EvaluatedMove> escapes =
            Actions.moveThreatenedPieceToSafety()
                .evaluate(
                    context,
                    underDefended
                );

        assertFalse(escapes.isEmpty());
    }

    @Test
    void detectsInitialKnightDevelopment() {
        ChessRulesEngine engine =
            ChessRulesEngines.standard();

        PositionView position =
            engine.initialPosition();

        BotContext context =
            context(engine, position);

        List<DevelopmentDetection> developments =
            Situations.developmentAvailable()
                .detect(context);

        assertEquals(4, developments.size());

        assertTrue(
            developments.stream()
                .anyMatch(detection ->
                    detection.move().equals(
                        Move.fromUci("g1f3")
                    )
                )
        );

        assertTrue(
            developments.stream()
                .anyMatch(detection ->
                    detection.move().equals(
                        Move.fromUci("b1c3")
                    )
                )
        );

        List<EvaluatedMove> candidates =
            Actions.playBestDevelopment()
                .evaluate(
                    context,
                    developments
                );

        assertEquals(
            developments.size(),
            candidates.size()
        );

        candidates.forEach(candidate ->
            assertTrue(
                candidate.score().value()
                    >= 0.0
                    && candidate.score().value()
                    <= 10.0
            )
        );
    }

    @Test
    void detectsMovesThatImproveCenterControl() {
        ChessRulesEngine engine =
            ChessRulesEngines.standard();

        PositionView position =
            engine.initialPosition();

        BotContext context =
            context(engine, position);

        List<CenterImprovementDetection> improvements =
            Situations.centerImprovementAvailable()
                .detect(context);

        assertFalse(improvements.isEmpty());

        improvements.forEach(detection ->
            assertTrue(
                detection.gain() > 0.0
            )
        );

        assertTrue(
            improvements.stream()
                .anyMatch(detection ->
                    detection.move().equals(
                        Move.fromUci("e2e4")
                    )
                    || detection.move().equals(
                        Move.fromUci("d2d4")
                    )
                    || detection.move().equals(
                        Move.fromUci("g1f3")
                    )
                    || detection.move().equals(
                        Move.fromUci("b1c3")
                    )
                )
        );

        List<EvaluatedMove> candidates =
            Actions.playBestCenterImprovement()
                .evaluate(
                    context,
                    improvements
                );

        assertEquals(
            improvements.size(),
            candidates.size()
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
