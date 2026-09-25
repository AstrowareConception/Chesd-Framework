package fr.astroware.chess.tournament.match;

import fr.astroware.chess.bots.baseline.RandomBot;
import fr.astroware.chess.bots.baseline.TacticalBot;
import fr.astroware.chess.core.game.GameStatus;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class MatchRunnerTest {

    @Test
    void tacticalBotFinishesMateInOnePosition() {
        MatchResult result = new MatchRunner().play(
            TacticalBot::new,
            RandomBot::new,
            MatchConfiguration.fromFen(
                10,
                42L,
                "7k/8/5KQ1/8/8/8/8/8 w - - 0 1"
            )
        );

        assertEquals(MatchTermination.NATURAL, result.termination());
        assertEquals(
            GameStatus.WHITE_WINS,
            result.gameResult().orElseThrow().status()
        );
        assertEquals(1, result.playedMoves().size());
    }

    @Test
    void sameSeedProducesSameRandomOpeningSequence() {
        MatchConfiguration configuration =
            new MatchConfiguration(12, 12345L, java.util.Optional.empty());

        MatchRunner runner = new MatchRunner();

        List<?> first = runner.play(
            RandomBot::new,
            RandomBot::new,
            configuration
        ).moves();

        List<?> second = runner.play(
            RandomBot::new,
            RandomBot::new,
            configuration
        ).moves();

        assertFalse(first.isEmpty());
        assertEquals(first, second);
    }
}
