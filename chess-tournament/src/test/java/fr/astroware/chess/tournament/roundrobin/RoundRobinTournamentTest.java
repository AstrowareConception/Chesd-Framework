package fr.astroware.chess.tournament.roundrobin;

import fr.astroware.chess.bots.baseline.CautiousBot;
import fr.astroware.chess.bots.baseline.GreedyBot;
import fr.astroware.chess.bots.baseline.RandomBot;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoundRobinTournamentTest {

    @Test
    void playsEveryPairWithAlternatingColors() {
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
                    ),
                    new TournamentParticipant(
                        "cautious",
                        CautiousBot::new
                    )
                ),
                new RoundRobinConfiguration(
                    2,
                    4,
                    123L
                )
            );

        // 3 paires × 2 parties.
        assertEquals(6, result.matches().size());
        assertEquals(3, result.standings().size());

        result.standings().forEach(standing ->
            assertEquals(4, standing.played())
        );

        double distributedPoints =
            result.standings().stream()
                .mapToDouble(
                    TournamentStanding::points
                )
                .sum();

        // Chaque partie distribue toujours exactement 1 point.
        assertEquals(6.0, distributedPoints);
    }

    @Test
    void rejectsDuplicateParticipantKeys() {
        boolean rejected = false;

        try {
            new RoundRobinTournament().play(
                List.of(
                    new TournamentParticipant(
                        "same",
                        RandomBot::new
                    ),
                    new TournamentParticipant(
                        "same",
                        GreedyBot::new
                    )
                ),
                new RoundRobinConfiguration(
                    1,
                    2,
                    1L
                )
            );
        } catch (IllegalArgumentException exception) {
            rejected = true;
        }

        assertTrue(rejected);
    }
}
