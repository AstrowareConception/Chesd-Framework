package fr.astroware.chess.tournament.roundrobin;

import fr.astroware.chess.bots.baseline.CautiousBot;
import fr.astroware.chess.bots.baseline.GreedyBot;
import fr.astroware.chess.bots.baseline.RandomBot;
import fr.astroware.chess.tournament.execution.IsolatedBotSettings;
import org.junit.jupiter.api.Test;

import java.time.Duration;
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
    void sameSeedProducesSameTournamentGamesAndRanking() {
        List<TournamentParticipant> participants =
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
            );

        RoundRobinConfiguration configuration =
            new RoundRobinConfiguration(
                2,
                10,
                20260926L
            );

        RoundRobinTournament tournament =
            new RoundRobinTournament();

        RoundRobinResult first =
            tournament.play(
                participants,
                configuration
            );

        RoundRobinResult second =
            tournament.play(
                participants,
                configuration
            );

        assertEquals(
            first.matches().stream()
                .map(match -> match.moves())
                .toList(),
            second.matches().stream()
                .map(match -> match.moves())
                .toList()
        );

        assertEquals(
            stableStandings(first),
            stableStandings(second)
        );
    }

    @Test
    void isolatedParticipantsCanPlayRoundRobin() {
        IsolatedBotSettings settings =
            new IsolatedBotSettings(
                Duration.ofSeconds(5),
                Duration.ofSeconds(2),
                64
            );

        List<TournamentParticipant> participants =
            List.of(
                TournamentParticipant.isolated(
                    "random-a",
                    RandomBot.class,
                    new RandomBot().metadata(),
                    settings
                ),
                TournamentParticipant.isolated(
                    "random-b",
                    RandomBot.class,
                    new RandomBot().metadata(),
                    settings
                )
            );

        RoundRobinConfiguration configuration =
            new RoundRobinConfiguration(
                2,
                4,
                909L
            );

        RoundRobinTournament tournament =
            new RoundRobinTournament();

        RoundRobinResult result =
            tournament.play(
                participants,
                configuration
            );

        RoundRobinResult repeated =
            tournament.play(
                participants,
                configuration
            );

        assertEquals(2, result.matches().size());
        assertEquals(2, result.standings().size());

        result.matches().forEach(match ->
            assertTrue(match.incident().isEmpty())
        );

        result.standings().forEach(standing ->
            assertEquals(2, standing.played())
        );

        assertEquals(
            result.matches().stream()
                .map(match -> match.moves())
                .toList(),
            repeated.matches().stream()
                .map(match -> match.moves())
                .toList()
        );

        assertEquals(
            stableStandings(result),
            stableStandings(repeated)
        );
    }

    private static List<String> stableStandings(
        RoundRobinResult result
    ) {
        return result.standings().stream()
            .map(standing ->
                standing.bot().botName()
                    + "|"
                    + standing.points()
                    + "|"
                    + standing.wins()
                    + "|"
                    + standing.draws()
                    + "|"
                    + standing.losses()
                    + "|"
                    + standing.forfeits()
                    + "|"
                    + standing.technicalDraws()
            )
            .toList();
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
