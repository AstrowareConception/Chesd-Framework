package fr.astroware.chess.tournament.roundrobin;

import java.util.Locale;
import java.util.Objects;

/**
 * Export CSV UTF-8 du classement final.
 */
public final class StandingsCsvExporter {

    public String export(RoundRobinResult result) {
        Objects.requireNonNull(
            result,
            "result must not be null"
        );

        StringBuilder csv = new StringBuilder();

        csv.append(
            "rank,bot,author,played,wins,draws,technical_draws,losses,points,average_decision_ms"
        ).append(System.lineSeparator());

        int rank = 1;

        for (TournamentStanding standing
            : result.standings()) {

            csv.append(rank++)
                .append(',')
                .append(csv(standing.bot().botName()))
                .append(',')
                .append(csv(standing.bot().authorName()))
                .append(',')
                .append(standing.played())
                .append(',')
                .append(standing.wins())
                .append(',')
                .append(standing.draws())
                .append(',')
                .append(standing.technicalDraws())
                .append(',')
                .append(standing.losses())
                .append(',')
                .append(
                    String.format(
                        Locale.ROOT,
                        "%.1f",
                        standing.points()
                    )
                )
                .append(',')
                .append(
                    String.format(
                        Locale.ROOT,
                        "%.3f",
                        standing.averageDecisionMillis()
                    )
                )
                .append(System.lineSeparator());
        }

        return csv.toString();
    }

    private static String csv(String value) {
        String escaped = value.replace(
            "\"",
            "\"\""
        );

        return "\"" + escaped + "\"";
    }
}
