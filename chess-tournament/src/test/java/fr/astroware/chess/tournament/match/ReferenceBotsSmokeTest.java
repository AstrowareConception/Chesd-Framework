package fr.astroware.chess.tournament.match;

import fr.astroware.chess.bots.baseline.ChameleonBot;
import fr.astroware.chess.bots.baseline.GuardianBot;
import fr.astroware.chess.bots.baseline.LookaheadBot;
import fr.astroware.chess.bots.baseline.PressureBot;
import fr.astroware.chess.bots.baseline.PositionalBot;
import fr.astroware.chess.bots.baseline.TacticalBot;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReferenceBotsSmokeTest {

    @Test
    void pressureAndChameleonCanPlayTogether() {
        MatchResult result = new MatchRunner().play(
            PressureBot::new,
            ChameleonBot::new,
            new MatchConfiguration(
                8,
                20260925L,
                java.util.Optional.empty()
            )
        );

        assertFalse(result.playedMoves().isEmpty());
        assertTrue(result.pliesPlayed() <= 8);
    }

    @Test
    void positionalAndPressureCanPlayTogether() {
        MatchResult result = new MatchRunner().play(
            PositionalBot::new,
            PressureBot::new,
            new MatchConfiguration(
                8,
                987654L,
                java.util.Optional.empty()
            )
        );

        assertFalse(result.playedMoves().isEmpty());
        assertTrue(result.pliesPlayed() <= 8);
    }

    @Test
    void positionalAndLookaheadCanPlayTogether() {
        MatchResult result = new MatchRunner().play(
            PositionalBot::new,
            LookaheadBot::new,
            new MatchConfiguration(
                6,
                515151L,
                java.util.Optional.empty()
            )
        );

        assertFalse(result.playedMoves().isEmpty());
        assertTrue(result.pliesPlayed() <= 6);
    }

    @Test
    void tacticalAndGuardianCanPlayTogether() {
        MatchResult result = new MatchRunner().play(
            TacticalBot::new,
            GuardianBot::new,
            new MatchConfiguration(
                8,
                424242L,
                java.util.Optional.empty()
            )
        );

        assertFalse(result.playedMoves().isEmpty());
        assertTrue(result.pliesPlayed() <= 8);
    }
}
