package fr.astroware.chess.tournament.roundrobin;

import fr.astroware.chess.bot.api.BotMetadata;
import fr.astroware.chess.core.game.GameStatus;
import fr.astroware.chess.core.model.Color;
import fr.astroware.chess.tournament.match.MatchConfiguration;
import fr.astroware.chess.tournament.match.MatchResult;
import fr.astroware.chess.tournament.match.MatchRunner;
import fr.astroware.chess.tournament.match.MatchTermination;
import fr.astroware.chess.tournament.match.PlayedMove;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Organise un tournoi toutes rondes entre plusieurs bots.
 *
 * <p>Chaque paire joue le nombre demandé de parties. Les couleurs alternent
 * automatiquement d'une partie à l'autre.</p>
 */
public final class RoundRobinTournament {

    private final MatchRunner matchRunner;

    public RoundRobinTournament() {
        this(new MatchRunner());
    }

    public RoundRobinTournament(MatchRunner matchRunner) {
        this.matchRunner = Objects.requireNonNull(
            matchRunner,
            "matchRunner must not be null"
        );
    }

    public RoundRobinResult play(
        List<TournamentParticipant> participants,
        RoundRobinConfiguration configuration
    ) {
        Objects.requireNonNull(
            participants,
            "participants must not be null"
        );
        Objects.requireNonNull(
            configuration,
            "configuration must not be null"
        );

        List<TournamentParticipant> roster =
            List.copyOf(participants);

        if (roster.size() < 2) {
            throw new IllegalArgumentException(
                "A tournament requires at least two participants"
            );
        }

        ensureUniqueKeys(roster);

        Map<String, StandingAccumulator> standings =
            new LinkedHashMap<>();

        for (TournamentParticipant participant : roster) {
            BotMetadata metadata =
                participant.factory().create().metadata();

            standings.put(
                participant.key(),
                new StandingAccumulator(metadata)
            );
        }

        List<MatchResult> matches =
            new ArrayList<>();

        int matchIndex = 0;

        for (int first = 0; first < roster.size(); first++) {
            for (
                int second = first + 1;
                second < roster.size();
                second++
            ) {
                TournamentParticipant a = roster.get(first);
                TournamentParticipant b = roster.get(second);

                for (
                    int game = 0;
                    game < configuration.gamesPerPair();
                    game++
                ) {
                    boolean aIsWhite = game % 2 == 0;

                    TournamentParticipant white =
                        aIsWhite ? a : b;
                    TournamentParticipant black =
                        aIsWhite ? b : a;

                    long seed = deriveSeed(
                        configuration.baseSeed(),
                        matchIndex
                    );

                    MatchResult match = matchRunner.play(
                        white.factory(),
                        black.factory(),
                        new MatchConfiguration(
                            configuration.maxPlies(),
                            seed,
                            java.util.Optional.empty()
                        )
                    );

                    matches.add(match);

                    updateStandings(
                        standings.get(white.key()),
                        standings.get(black.key()),
                        match
                    );

                    matchIndex++;
                }
            }
        }

        List<TournamentStanding> ranking =
            standings.values().stream()
                .map(StandingAccumulator::snapshot)
                .sorted(
                    Comparator
                        .comparingDouble(
                            TournamentStanding::points
                        )
                        .reversed()
                        .thenComparing(
                            Comparator.comparingInt(
                                TournamentStanding::wins
                            ).reversed()
                        )
                        .thenComparingInt(
                            TournamentStanding::technicalDraws
                        )
                        .thenComparingDouble(
                            TournamentStanding::averageDecisionMillis
                        )
                        .thenComparing(
                            standing ->
                                standing.bot().botName()
                        )
                )
                .toList();

        return new RoundRobinResult(
            matches,
            ranking
        );
    }

    private static void updateStandings(
        StandingAccumulator white,
        StandingAccumulator black,
        MatchResult match
    ) {
        white.played++;
        black.played++;

        accumulateTimes(
            white,
            match,
            Color.WHITE
        );
        accumulateTimes(
            black,
            match,
            Color.BLACK
        );

        if (match.termination()
            == MatchTermination.MOVE_LIMIT) {

            white.technicalDraws++;
            black.technicalDraws++;
            white.points += 0.5;
            black.points += 0.5;
            return;
        }

        GameStatus status =
            match.gameResult()
                .orElseThrow()
                .status();

        match.incident().ifPresent(incident -> {
            if (incident.offenderColor() == Color.WHITE) {
                white.forfeits++;
            } else {
                black.forfeits++;
            }
        });

        switch (status) {
            case WHITE_WINS -> {
                white.wins++;
                black.losses++;
                white.points += 1.0;
            }
            case BLACK_WINS -> {
                black.wins++;
                white.losses++;
                black.points += 1.0;
            }
            case DRAW -> {
                white.draws++;
                black.draws++;
                white.points += 0.5;
                black.points += 0.5;
            }
            case ONGOING -> throw new IllegalStateException(
                "A naturally finished match cannot remain ongoing"
            );
        }
    }

    private static void accumulateTimes(
        StandingAccumulator standing,
        MatchResult match,
        Color color
    ) {
        List<PlayedMove> moves =
            match.playedMoves().stream()
                .filter(move -> move.color() == color)
                .toList();

        standing.decisionCount += moves.size();
        standing.totalDecisionMillis += moves.stream()
            .mapToDouble(PlayedMove::decisionMillis)
            .sum();
    }

    private static long deriveSeed(
        long baseSeed,
        int matchIndex
    ) {
        return baseSeed
            ^ (
                0x9E3779B97F4A7C15L
                    * (matchIndex + 1L)
            );
    }

    private static void ensureUniqueKeys(
        List<TournamentParticipant> participants
    ) {
        long distinct = participants.stream()
            .map(TournamentParticipant::key)
            .distinct()
            .count();

        if (distinct != participants.size()) {
            throw new IllegalArgumentException(
                "Tournament participant keys must be unique"
            );
        }
    }

    private static final class StandingAccumulator {

        private final BotMetadata metadata;

        private int played;
        private int wins;
        private int draws;
        private int losses;
        private int technicalDraws;
        private int forfeits;
        private double points;
        private double totalDecisionMillis;
        private int decisionCount;

        private StandingAccumulator(
            BotMetadata metadata
        ) {
            this.metadata = metadata;
        }

        private TournamentStanding snapshot() {
            double average = decisionCount == 0
                ? 0.0
                : totalDecisionMillis / decisionCount;

            return new TournamentStanding(
                metadata,
                played,
                wins,
                draws,
                losses,
                forfeits,
                technicalDraws,
                points,
                average
            );
        }
    }
}
