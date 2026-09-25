package fr.astroware.chess.tournament.console;

import fr.astroware.chess.tournament.roundrobin.RoundRobinResult;
import fr.astroware.chess.tournament.roundrobin.TournamentStanding;

import java.io.PrintStream;
import java.util.Objects;

/**
 * Affichage console synthétique d'un tournoi toutes rondes.
 */
public final class ConsoleTournamentReporter {

    private final PrintStream out;

    public ConsoleTournamentReporter() {
        this(System.out);
    }

    public ConsoleTournamentReporter(PrintStream out) {
        this.out = Objects.requireNonNull(
            out,
            "out must not be null"
        );
    }

    public void print(RoundRobinResult result) {
        Objects.requireNonNull(
            result,
            "result must not be null"
        );

        out.println();
        out.println(
            "=========================================================================="
        );
        out.println(
            "                       TOURNOI CHESS FRAMEWORK"
        );
        out.println(
            "=========================================================================="
        );
        out.printf(
            "Parties jouées : %d%n%n",
            result.matches().size()
        );

        out.printf(
            "%-4s %-22s %5s %4s %4s %4s %5s %7s %10s%n",
            "#",
            "Bot",
            "Pts",
            "V",
            "N",
            "D",
            "NT",
            "Parties",
            "Moy. ms"
        );

        out.println(
            "--------------------------------------------------------------------------"
        );

        int rank = 1;

        for (TournamentStanding standing
            : result.standings()) {

            out.printf(
                "%-4d %-22s %5.1f %4d %4d %4d %5d %7d %10.1f%n",
                rank++,
                standing.bot().botName(),
                standing.points(),
                standing.wins(),
                standing.draws(),
                standing.losses(),
                standing.technicalDraws(),
                standing.played(),
                standing.averageDecisionMillis()
            );
        }

        out.println();
        out.println(
            "NT = nulles techniques (limite de demi-coups atteinte)"
        );
        out.println(
            "Barème : victoire 1 pt, nulle 0,5 pt, défaite 0 pt"
        );
        out.println(
            "=========================================================================="
        );
    }
}
