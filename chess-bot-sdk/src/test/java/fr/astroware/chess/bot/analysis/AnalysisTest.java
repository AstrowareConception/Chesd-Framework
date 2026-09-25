package fr.astroware.chess.bot.analysis;

import fr.astroware.chess.bot.api.BotContext;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnalysisTest {

    @Test
    void knightAttacksExpectedSquares() {
        PlacedPiece knight = piece(Color.WHITE, PieceType.KNIGHT, "d4");

        Analysis analysis = context(
            Color.WHITE,
            Map.of(knight.square(), knight.piece()),
            List.of()
        ).analysis();

        assertTrue(
            analysis.attackMap().attacksFrom(knight).contains(Square.from("f5"))
        );
        assertTrue(
            analysis.attackMap().attacksFrom(knight).contains(Square.from("b3"))
        );
        assertEquals(8, analysis.attackMap().attacksFrom(knight).size());
    }

    @Test
    void slidingPieceStopsAtFirstObstacle() {
        PlacedPiece rook = piece(Color.WHITE, PieceType.ROOK, "a1");
        PlacedPiece blocker = piece(Color.WHITE, PieceType.PAWN, "a3");

        Analysis analysis = context(
            Color.WHITE,
            Map.of(
                rook.square(), rook.piece(),
                blocker.square(), blocker.piece()
            ),
            List.of()
        ).analysis();

        assertTrue(
            analysis.attackMap().attacksFrom(rook).contains(Square.from("a3"))
        );
        assertFalse(
            analysis.attackMap().attacksFrom(rook).contains(Square.from("a4"))
        );
    }

    @Test
    void detectsHangingPiece() {
        PlacedPiece blackQueen = piece(Color.BLACK, PieceType.QUEEN, "d5");
        PlacedPiece whiteBishop = piece(Color.WHITE, PieceType.BISHOP, "b3");

        Analysis analysis = context(
            Color.WHITE,
            Map.of(
                blackQueen.square(), blackQueen.piece(),
                whiteBishop.square(), whiteBishop.piece()
            ),
            List.of(Move.of("b3", "d5"))
        ).analysis();

        assertTrue(analysis.isAttacked(blackQueen));
        assertFalse(analysis.isDefended(blackQueen));
        assertTrue(analysis.isHanging(blackQueen));
    }

    @Test
    void computesMaterialBalance() {
        Map<Square, Piece> board = Map.of(
            Square.from("a1"), new Piece(Color.WHITE, PieceType.ROOK),
            Square.from("d1"), new Piece(Color.WHITE, PieceType.QUEEN),
            Square.from("a8"), new Piece(Color.BLACK, PieceType.ROOK)
        );

        Analysis analysis = context(
            Color.WHITE,
            board,
            List.of()
        ).analysis();

        assertEquals(14, analysis.material(Color.WHITE));
        assertEquals(5, analysis.material(Color.BLACK));
        assertEquals(
            9,
            analysis.materialBalance().advantageFor(Color.WHITE)
        );
    }

    private static PlacedPiece piece(
        Color color,
        PieceType type,
        String square
    ) {
        return new PlacedPiece(
            new Piece(color, type),
            Square.from(square)
        );
    }

    private static BotContext context(
        Color myColor,
        Map<Square, Piece> board,
        List<Move> legalMoves
    ) {
        return new TestContext(myColor, board, legalMoves);
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
