package fr.astroware.chess.bots.baseline;

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
import static org.junit.jupiter.api.Assertions.assertTrue;

class RandomBotTest {

    @Test
    void exposesItsIdentity() {
        RandomBot bot = new RandomBot();

        assertEquals("Random Bot", bot.metadata().botName());
        assertEquals("AstroWare Conception", bot.metadata().authorName());
    }

    @Test
    void alwaysReturnsOneOfTheLegalMoves() {
        List<Move> legalMoves = List.of(
            Move.of("e2", "e4"),
            Move.of("d2", "d4"),
            Move.of("g1", "f3")
        );

        RandomBot bot = new RandomBot();
        BotDecision decision = bot.decide(new TestContext(legalMoves));

        assertTrue(legalMoves.contains(decision.move()));
        assertEquals("Jouer au hasard", decision.trace().getFirst().ruleName());
    }

    private record TestContext(List<Move> legalMoves) implements BotContext {

        @Override
        public Color myColor() {
            return Color.WHITE;
        }

        @Override
        public PositionView position() {
            return EMPTY_POSITION;
        }

        @Override
        public RandomGenerator random() {
            return new Random(42L);
        }
    }

    private static final PositionView EMPTY_POSITION = new PositionView() {
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
        public Color sideToMove() {
            return Color.WHITE;
        }

        @Override
        public Optional<Move> lastMove() {
            return Optional.empty();
        }

        @Override
        public String fen() {
            return "";
        }
    };
}
