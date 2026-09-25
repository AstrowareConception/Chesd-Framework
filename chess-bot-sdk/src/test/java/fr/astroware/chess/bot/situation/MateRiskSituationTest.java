package fr.astroware.chess.bot.situation;

import fr.astroware.chess.bot.action.Actions;
import fr.astroware.chess.bot.api.BotContext;
import fr.astroware.chess.bot.evaluation.EvaluatedMove;
import fr.astroware.chess.bot.situation.detection.MateRiskDetection;
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

class MateRiskSituationTest {

    @Test
    void detectsMovesThatAllowFoolsMatePattern() {
        ChessRulesEngine engine = ChessRulesEngines.standard();
        PositionView position = engine.fromFen(
            "rnbqkbnr/pppp1ppp/8/4p3/6P1/5P2/PPPPP2P/RNBQKBNR w KQkq - 0 3"
        );

        BotContext context = new EngineContext(
            Color.WHITE,
            position,
            engine.legalMoves(position)
        );

        List<MateRiskDetection> risks =
            Situations.mateInOneRisk().detect(context);

        assertFalse(risks.isEmpty());

        List<EvaluatedMove> safeCandidates =
            Actions.avoidMateInOne().evaluate(
                context,
                risks
            );

        assertFalse(safeCandidates.isEmpty());

        for (EvaluatedMove candidate : safeCandidates) {
            var after = context.analysis().after(candidate.move());

            assertTrue(
                after.analysis().mateInOneMoves().isEmpty()
            );
        }
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
