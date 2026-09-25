package fr.astroware.chess.bot.strategy;

import fr.astroware.chess.bot.evaluation.EvaluatedMove;
import fr.astroware.chess.core.model.Move;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class StrategyProfileTest {

    @Test
    void defensiveProfilePrefersSaferMove() {
        EvaluatedMove safe = EvaluatedMove.strategic(
            Move.of("g1", "f3"),
            7.0,
            3.0,
            9.0,
            2.0,
            "Développement solide"
        );

        EvaluatedMove risky = EvaluatedMove.strategic(
            Move.of("f2", "f4"),
            7.0,
            9.0,
            2.0,
            9.0,
            "Attaque spéculative"
        );

        StrategyProfile defensive = StrategyProfiles.defensive();

        assertTrue(
            defensive.preferenceScore(safe).value()
                > defensive.preferenceScore(risky).value()
        );
    }

    @Test
    void adventurousProfileCanPreferRiskyMove() {
        EvaluatedMove safe = EvaluatedMove.strategic(
            Move.of("g1", "f3"),
            7.0,
            3.0,
            9.0,
            2.0,
            "Développement solide"
        );

        EvaluatedMove risky = EvaluatedMove.strategic(
            Move.of("f2", "f4"),
            7.0,
            9.0,
            2.0,
            9.0,
            "Attaque spéculative"
        );

        StrategyProfile adventurous = StrategyProfiles.adventurous();

        assertTrue(
            adventurous.preferenceScore(risky).value()
                > adventurous.preferenceScore(safe).value()
        );
    }
}
