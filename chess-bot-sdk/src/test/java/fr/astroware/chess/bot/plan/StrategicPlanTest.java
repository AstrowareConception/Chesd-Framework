package fr.astroware.chess.bot.plan;

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
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.random.RandomGenerator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StrategicPlanTest {

    @Test
    void takeCenterSuggestsCentralMoves() {
        StrategicPlan plan = Plans.takeCenter();

        Move e4 = Move.fromUci("e2e4");
        Move h3 = Move.fromUci("h2h3");

        List<EvaluatedMove> candidates = plan.candidates(
            new TestContext(
                Color.WHITE,
                List.of(e4, h3),
                Map.of()
            )
        );

        assertEquals(1, candidates.size());
        assertEquals(e4, candidates.getFirst().move());
    }

    @Test
    void castlePlanUsesCastleImmediatelyWhenLegal() {
        StrategicPlan plan = Plans.castleKingside();
        Move castle = Move.fromUci("e1g1");

        List<EvaluatedMove> candidates = plan.candidates(
            new TestContext(
                Color.WHITE,
                List.of(castle, Move.fromUci("g1f3")),
                Map.of()
            )
        );

        assertEquals(1, candidates.size());
        assertEquals(castle, candidates.getFirst().move());
        assertTrue(candidates.getFirst().safety().value() > 9.0);
    }

    private record TestContext(
        Color myColor,
        List<Move> legalMoves,
        Map<Square, Piece> pieces
    ) implements BotContext {

        @Override
        public PositionView position() {
            return new MapPosition(myColor, pieces);
        }

        @Override
        public RandomGenerator random() {
            return new Random(42L);
        }
    }

    private record MapPosition(
        Color sideToMove,
        Map<Square, Piece> piecesBySquare
    ) implements PositionView {

        @Override
        public Optional<Piece> pieceAt(Square square) {
            return Optional.ofNullable(piecesBySquare.get(square));
        }

        @Override
        public List<PlacedPiece> pieces() {
            return piecesBySquare.entrySet().stream()
                .map(entry -> new PlacedPiece(entry.getValue(), entry.getKey()))
                .toList();
        }

        @Override
        public List<PlacedPiece> pieces(Color color) {
            return pieces().stream()
                .filter(piece -> piece.piece().color() == color)
                .toList();
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
