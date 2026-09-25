package fr.astroware.chess.bots.examples;

import fr.astroware.chess.bot.api.BotContext;
import fr.astroware.chess.bot.api.BotDecision;
import fr.astroware.chess.core.model.Color;
import fr.astroware.chess.core.model.Move;
import fr.astroware.chess.core.model.Piece;
import fr.astroware.chess.core.model.PlacedPiece;
import fr.astroware.chess.core.model.PositionView;
import fr.astroware.chess.core.model.Square;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.random.RandomGenerator;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SolidPlannerBotTest {

    @Test
    void playsLondonFirstMoveAsWhite() {
        Move d4 = Move.fromUci("d2d4");

        BotDecision decision = new SolidPlannerBot().decide(
            new TestContext(
                Color.WHITE,
                List.of(),
                List.of(d4, Move.fromUci("e2e4"))
            )
        );

        assertEquals(d4, decision.move());
        assertEquals(
            "Ouverture : Système de Londres",
            decision.trace().getFirst().ruleName()
        );
    }

    @Test
    void playsScandinavianFirstMoveAsBlackAfterE4() {
        Move d5 = Move.fromUci("d7d5");

        BotDecision decision = new SolidPlannerBot().decide(
            new TestContext(
                Color.BLACK,
                List.of(Move.fromUci("e2e4")),
                List.of(d5, Move.fromUci("e7e5"))
            )
        );

        assertEquals(d5, decision.move());
        assertEquals(
            "Ouverture : Défense Scandinave",
            decision.trace().get(1).ruleName()
        );
    }

    private record TestContext(
        Color myColor,
        List<Move> moveHistory,
        List<Move> legalMoves
    ) implements BotContext {

        @Override
        public PositionView position() {
            return new EmptyPosition(myColor);
        }

        @Override
        public RandomGenerator random() {
            return new Random(42L);
        }
    }

    private record EmptyPosition(Color sideToMove) implements PositionView {

        @Override
        public Optional<Piece> pieceAt(Square square) {
            return Optional.empty();
        }

        @Override
        public List<PlacedPiece> pieces() {
            return List.of();
        }

        @Override
        public List<PlacedPiece> pieces(Color color) {
            return List.of();
        }

        @Override
        public Optional<Move> lastMove() {
            return Optional.empty();
        }

        @Override
        public String fen() {
            return "";
        }
    }
}
