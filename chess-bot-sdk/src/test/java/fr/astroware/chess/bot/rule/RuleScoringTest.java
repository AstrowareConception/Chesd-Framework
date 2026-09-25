package fr.astroware.chess.bot.rule;

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

class RuleScoringTest {

    @Test
    void selectsHighestScoredLegalCandidate() {
        Move average = Move.of("e2", "e4");
        Move best = Move.of("d2", "d4");
        Move illegalButHighlyScored = Move.of("a2", "a5");

        BotContext context = new TestContext(List.of(average, best));

        Situation<PresenceDetection> situation =
            ignored -> List.of(PresenceDetection.INSTANCE);

        Action<PresenceDetection> action = (ignored, detections) -> List.of(
            EvaluatedMove.of(average, 6.0, "Coup correct"),
            EvaluatedMove.of(best, 8.5, "Meilleur contrôle du centre"),
            EvaluatedMove.of(
                illegalButHighlyScored,
                10.0,
                "Cette note ne compte pas : le coup est illégal"
            )
        );

        Rule<PresenceDetection> rule =
            Rule.of("Choisir le meilleur candidat", situation, action);

        RuleAttempt attempt = rule.evaluate(context);

        assertEquals(AttemptStatus.SELECTED, attempt.status());
        assertEquals(3, attempt.candidates().size());
        assertEquals(
            best,
            attempt.selectedMove().orElseThrow().move()
        );
        assertEquals(
            8.5,
            attempt.selectedMove().orElseThrow().score().value()
        );
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
