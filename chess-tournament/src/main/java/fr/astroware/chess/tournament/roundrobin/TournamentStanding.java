package fr.astroware.chess.tournament.roundrobin;

import fr.astroware.chess.bot.api.BotMetadata;

import java.util.Objects;

/**
 * Ligne de classement d'un tournoi.
 */
public record TournamentStanding(
    BotMetadata bot,
    int played,
    int wins,
    int draws,
    int losses,
    int forfeits,
    int technicalDraws,
    double points,
    double averageDecisionMillis
) {

    public TournamentStanding {
        Objects.requireNonNull(bot, "bot must not be null");

        if (played < 0
            || wins < 0
            || draws < 0
            || losses < 0
            || forfeits < 0
            || technicalDraws < 0) {
            throw new IllegalArgumentException(
                "standing counters must be non-negative"
            );
        }

        if (forfeits > losses) {
            throw new IllegalArgumentException(
                "forfeits cannot exceed losses"
            );
        }

        if (points < 0.0
            || averageDecisionMillis < 0.0) {
            throw new IllegalArgumentException(
                "points and time must be non-negative"
            );
        }
    }
}
