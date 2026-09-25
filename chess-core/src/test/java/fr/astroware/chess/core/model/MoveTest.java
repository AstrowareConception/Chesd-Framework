package fr.astroware.chess.core.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MoveTest {

    @Test
    void createsNormalMove() {
        Move move = Move.of("e2", "e4");

        assertEquals(Square.from("e2"), move.from());
        assertEquals(Square.from("e4"), move.to());
        assertTrue(move.promotion().isEmpty());
    }

    @Test
    void createsPromotion() {
        Move move = Move.promotion("e7", "e8", PieceType.QUEEN);

        assertEquals(PieceType.QUEEN, move.promotion().orElseThrow());
    }

    @Test
    void parsesAndWritesUciNotation() {
        Move normal = Move.fromUci("e2e4");
        Move promotion = Move.fromUci("e7e8q");

        assertEquals(Move.of("e2", "e4"), normal);
        assertEquals("e2e4", normal.toUci());
        assertEquals(PieceType.QUEEN, promotion.promotion().orElseThrow());
        assertEquals("e7e8q", promotion.toUci());
    }

    @Test
    void rejectsInvalidUciNotation() {
        assertThrows(
            IllegalArgumentException.class,
            () -> Move.fromUci("e2e9")
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> Move.fromUci("e7e8x")
        );
    }

    @Test
    void rejectsInvalidPromotionType() {
        assertThrows(
            IllegalArgumentException.class,
            () -> Move.promotion("e7", "e8", PieceType.KING)
        );
    }
}
