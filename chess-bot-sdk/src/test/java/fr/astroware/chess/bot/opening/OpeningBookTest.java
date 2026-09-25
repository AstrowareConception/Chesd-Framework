package fr.astroware.chess.bot.opening;

import fr.astroware.chess.bot.api.BotContext;
import fr.astroware.chess.bot.evaluation.EvaluatedMove;
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

class OpeningBookTest {

    @Test
    void londonSuggestsD4AtGameStart() {
        OpeningBook london = Openings.londonSystem();
        Move d4 = Move.fromUci("d2d4");

        List<EvaluatedMove> candidates = london.candidates(
            new TestContext(
                Color.WHITE,
                Color.WHITE,
                List.of(),
                List.of(d4)
            )
        );

        assertEquals(1, candidates.size());
        assertEquals(d4, candidates.getFirst().move());
    }

    @Test
    void londonContinuesWhileHistoryMatches() {
        OpeningBook london = Openings.londonSystem();
        Move nf3 = Move.fromUci("g1f3");

        List<EvaluatedMove> candidates = london.candidates(
            new TestContext(
                Color.WHITE,
                Color.WHITE,
                List.of(
                    Move.fromUci("d2d4"),
                    Move.fromUci("d7d5")
                ),
                List.of(nf3)
            )
        );

        assertTrue(
            candidates.stream().anyMatch(candidate -> candidate.move().equals(nf3))
        );
    }

    @Test
    void openingStopsWhenOpponentLeavesKnownLines() {
        OpeningBook london = Openings.londonSystem();

        List<EvaluatedMove> candidates = london.candidates(
            new TestContext(
                Color.WHITE,
                Color.WHITE,
                List.of(
                    Move.fromUci("d2d4"),
                    Move.fromUci("a7a6")
                ),
                List.of(Move.fromUci("g1f3"))
            )
        );

        assertTrue(candidates.isEmpty());
    }

    private record TestContext(
        Color myColor,
        Color sideToMove,
        List<Move> moveHistory,
        List<Move> legalMoves
    ) implements BotContext {

        @Override
        public PositionView position() {
            return new EmptyPosition(sideToMove);
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
