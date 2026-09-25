package fr.astroware.chess.tournament.roundrobin;

import fr.astroware.chess.bots.baseline.GreedyBot;
import fr.astroware.chess.bots.baseline.RandomBot;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TournamentExportTest {

    @Test
    void exportsMultiGamePgnAndCsvStandings() {
        RoundRobinResult result =
            new RoundRobinTournament().play(
                List.of(
                    new TournamentParticipant(
                        "random",
                        RandomBot::new
                    ),
                    new TournamentParticipant(
                        "greedy",
                        GreedyBot::new
                    )
                ),
                new RoundRobinConfiguration(
                    2,
                    4,
                    77L
                )
            );

        String pgn =
            new RoundRobinPgnExporter().export(result);

        String csv =
            new StandingsCsvExporter().export(result);

        assertTrue(
            pgn.contains("[White "Random Bot"]")
                || pgn.contains("[Black "Random Bot"]")
        );

        assertTrue(
            pgn.contains("[White "Greedy Bot"]")
                || pgn.contains("[Black "Greedy Bot"]")
        );

        assertTrue(
            csv.startsWith(
                "rank,bot,author,played"
            )
        );

        assertTrue(csv.contains(""Random Bot""));
        assertTrue(csv.contains(""Greedy Bot""));
    }
}
