package fr.astroware.chess.tournament.roundrobin;

import fr.astroware.chess.tournament.match.MatchResult;

import java.util.List;
import java.util.Objects;

/**
 * Résultat complet d'un tournoi toutes rondes.
 */
public record RoundRobinResult(
    List<MatchResult> matches,
    List<TournamentStanding> standings
) {

    public RoundRobinResult {
        matches = List.copyOf(
            Objects.requireNonNull(matches, "matches must not be null")
        );
        standings = List.copyOf(
            Objects.requireNonNull(standings, "standings must not be null")
        );
    }
}
