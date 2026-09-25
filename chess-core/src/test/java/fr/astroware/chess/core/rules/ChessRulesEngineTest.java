package fr.astroware.chess.core.rules;

import fr.astroware.chess.core.game.GameStatus;
import fr.astroware.chess.core.model.Color;
import fr.astroware.chess.core.model.Move;
import fr.astroware.chess.core.model.PieceType;
import fr.astroware.chess.core.model.PositionView;
import fr.astroware.chess.core.model.Square;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChessRulesEngineTest {

    private final ChessRulesEngine engine = ChessRulesEngines.standard();

    @Test
    void initialPositionHasTwentyLegalMoves() {
        PositionView position = engine.initialPosition();

        assertEquals(Color.WHITE, position.sideToMove());
        assertEquals(20, engine.legalMoves(position).size());
        assertTrue(
            position.pieceAt(Square.from("e1"))
                .filter(piece -> piece.type() == PieceType.KING)
                .isPresent()
        );
    }

    @Test
    void playsMoveWithoutMutatingPreviousPosition() {
        PositionView initial = engine.initialPosition();
        PositionView afterE4 = engine.play(
            initial,
            Move.fromUci("e2e4")
        );

        assertEquals(Color.WHITE, initial.sideToMove());
        assertEquals(Color.BLACK, afterE4.sideToMove());
        assertTrue(initial.pieceAt(Square.from("e2")).isPresent());
        assertTrue(afterE4.pieceAt(Square.from("e2")).isEmpty());
        assertTrue(afterE4.pieceAt(Square.from("e4")).isPresent());
        assertEquals(
            Move.fromUci("e2e4"),
            afterE4.lastMove().orElseThrow()
        );
    }

    @Test
    void rejectsIllegalMove() {
        PositionView position = engine.initialPosition();

        assertThrows(
            IllegalChessMoveException.class,
            () -> engine.play(position, Move.fromUci("e2e5"))
        );
    }

    @Test
    void detectsFoolsMate() {
        PositionView position = engine.initialPosition();

        position = engine.play(position, Move.fromUci("f2f3"));
        position = engine.play(position, Move.fromUci("e7e5"));
        position = engine.play(position, Move.fromUci("g2g4"));
        position = engine.play(position, Move.fromUci("d8h4"));

        assertEquals(GameStatus.BLACK_WINS, engine.result(position).status());
        assertTrue(engine.result(position).isOver());
    }

    @Test
    void loadsFenAndDetectsCheck() {
        PositionView position = engine.fromFen(
            "4k3/8/8/8/8/8/4Q3/4K3 b - - 0 1"
        );

        assertTrue(engine.isKingAttacked(position));
        assertFalse(engine.result(position).isOver());
    }
}
