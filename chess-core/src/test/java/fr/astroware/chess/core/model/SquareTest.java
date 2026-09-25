package fr.astroware.chess.core.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SquareTest {

    @Test
    void parsesAlgebraicNotation() {
        Square square = Square.from("e4");

        assertEquals(BoardFile.E, square.file());
        assertEquals(BoardRank.FOUR, square.rank());
        assertEquals("e4", square.notation());
    }

    @Test
    void acceptsUppercaseNotation() {
        assertEquals(Square.from("a8"), Square.from("A8"));
    }

    @Test
    void rejectsInvalidNotation() {
        assertThrows(IllegalArgumentException.class, () -> Square.from("z9"));
        assertThrows(IllegalArgumentException.class, () -> Square.from("e"));
    }
}
