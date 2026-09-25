package fr.astroware.chess.bot.evaluation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EvaluationScoreTest {

    @Test
    void acceptsScoresBetweenZeroAndTen() {
        assertEquals(0.0, EvaluationScore.of(0.0).value());
        assertEquals(5.0, EvaluationScore.neutral().value());
        assertEquals(10.0, EvaluationScore.of(10.0).value());
    }

    @Test
    void rejectsScoresOutsideTheScale() {
        assertThrows(
            IllegalArgumentException.class,
            () -> EvaluationScore.of(-0.1)
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> EvaluationScore.of(10.1)
        );
    }
}
