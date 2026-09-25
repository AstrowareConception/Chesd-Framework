package fr.astroware.chess.bots.baseline;

import fr.astroware.chess.bot.api.BotContext;
import fr.astroware.chess.bot.api.BotDecision;
import fr.astroware.chess.core.model.Color;
import fr.astroware.chess.core.model.Move;
import fr.astroware.chess.core.model.Piece;
import fr.astroware.chess.core.model.PieceType;
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

class BerserkerBotTest {

    @Test
    void acceptsRiskToCaptureQueenInsteadOfHangingRook() {
        Move defendedQueen = Move.of("b3", "d5");
        Move hangingRook = Move.of("h1", "h8");

        BotContext context = new TestContext(
            Color.WHITE,
            Map.of(
                Square.from("b3"),
                new Piece(Color.WHITE, PieceType.BISHOP),
                Square.from("h1"),
                new Piece(Color.WHITE, PieceType.ROOK),
                Square.from("d5"),
                new Piece(Color.BLACK, PieceType.QUEEN),
                Square.from("g8"),
                new Piece(Color.BLACK, PieceType.BISHOP),
                Square.from("h8"),
                new Piece(Color.BLACK, PieceType.ROOK)
            ),
            List.of(defendedQueen, hangingRook)
        );

        BotDecision decision = new BerserkerBot().decide(context);

        assertEquals(defendedQueen, decision.move());
        assertEquals(
            "Capturer agressivement",
            decision.trace().getFirst().ruleName()
        );
    }

    private record TestContext(
        Color myColor,
        Map<Square, Piece> board,
        List<Move> legalMoves
    ) implements BotContext {

        @Override
        public PositionView position() {
            return new TestPosition(myColor, board);
        }

        @Override
        public RandomGenerator random() {
            return new Random(42L);
        }
    }

    private record TestPosition(
        Color sideToMove,
        Map<Square, Piece> board
    ) implements PositionView {

        @Override
        public Optional<Piece> pieceAt(Square square) {
            return Optional.ofNullable(board.get(square));
        }

        @Override
        public List<PlacedPiece> pieces() {
            return board.entrySet().stream()
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
