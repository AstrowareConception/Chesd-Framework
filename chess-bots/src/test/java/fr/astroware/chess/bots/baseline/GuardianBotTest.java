package fr.astroware.chess.bots.baseline;

import fr.astroware.chess.bot.api.BotContext;
import fr.astroware.chess.bot.api.BotDecision;
import fr.astroware.chess.bot.analysis.PositionProjection;
import fr.astroware.chess.core.model.Color;
import fr.astroware.chess.core.model.Move;
import fr.astroware.chess.core.model.Piece;
import fr.astroware.chess.core.model.PlacedPiece;
import fr.astroware.chess.core.model.PositionView;
import fr.astroware.chess.core.rules.ChessRulesEngine;
import fr.astroware.chess.core.rules.ChessRulesEngines;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;
import java.util.random.RandomGenerator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class GuardianBotTest {

    @Test
    void movesHangingQueenToARealSafeSquare() {
        ChessRulesEngine engine = ChessRulesEngines.standard();
        PositionView position = engine.fromFen(
            "3rk3/8/8/8/3Q4/8/8/4K3 w - - 0 1"
        );

        BotContext context = new EngineContext(
            Color.WHITE,
            position,
            engine.legalMoves(position)
        );

        BotDecision decision = new GuardianBot().decide(context);

        assertEquals(
            "Sauver une pièce pendue",
            decision.trace().stream()
                .filter(attempt -> attempt.selectedMove().isPresent())
                .findFirst()
                .orElseThrow()
                .ruleName()
        );

        PositionProjection projection =
            context.analysis().after(decision.move());

        Piece movedPiece = projection.position()
            .pieceAt(decision.move().to())
            .orElseThrow();

        PlacedPiece placed =
            new PlacedPiece(movedPiece, decision.move().to());

        assertFalse(projection.analysis().isHanging(placed));
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
