package fr.astroware.chess.tournament.pgn;

import fr.astroware.chess.bots.baseline.RandomBot;
import fr.astroware.chess.bots.baseline.TacticalBot;
import fr.astroware.chess.tournament.match.MatchConfiguration;
import fr.astroware.chess.tournament.match.MatchResult;
import fr.astroware.chess.tournament.match.MatchRunner;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PgnExporterTest {

    @Test
    void exportsImportablePgnWithResultAndFen() {
        MatchResult result = new MatchRunner().play(
            TacticalBot::new,
            RandomBot::new,
            MatchConfiguration.fromFen(
                10,
                42L,
                "7k/8/5KQ1/8/8/8/8/8 w - - 0 1"
            )
        );

        String pgn = new PgnExporter().export(result);

        assertTrue(pgn.contains("[White \"Tactical Bot\"]"));
        assertTrue(pgn.contains("[Black \"Random Bot\"]"));
        assertTrue(pgn.contains("[Result \"1-0\"]"));
        assertTrue(pgn.contains("[SetUp \"1\"]"));
        assertTrue(pgn.contains("[FEN \"7k/8/5KQ1/8/8/8/8/8 w - - 0 1\"]"));
        assertTrue(pgn.trim().endsWith("1-0"));
    }
}
