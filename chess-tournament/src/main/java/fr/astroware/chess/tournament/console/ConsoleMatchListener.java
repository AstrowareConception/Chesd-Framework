package fr.astroware.chess.tournament.console;

import fr.astroware.chess.bot.rule.AttemptStatus;
import fr.astroware.chess.bot.rule.RuleAttempt;
import fr.astroware.chess.core.game.GameStatus;
import fr.astroware.chess.core.model.Color;
import fr.astroware.chess.core.model.PositionView;
import fr.astroware.chess.tournament.match.MatchListener;
import fr.astroware.chess.tournament.match.MatchResult;
import fr.astroware.chess.tournament.match.PlayedMove;

import java.io.PrintStream;
import java.util.Comparator;
import java.util.Objects;

/**
 * Affichage console d'une partie en temps réel.
 *
 * <p>Le mode détaillé montre également les candidats envisagés par la règle
 * sélectionnée, avec leurs scores et leur caractère stratégique.</p>
 */
public final class ConsoleMatchListener implements MatchListener {

    private final PrintStream out;
    private final boolean verbose;

    public ConsoleMatchListener() {
        this(System.out, true);
    }

    public ConsoleMatchListener(PrintStream out, boolean verbose) {
        this.out = Objects.requireNonNull(out, "out must not be null");
        this.verbose = verbose;
    }

    @Override
    public void onMatchStarted(
        fr.astroware.chess.bot.api.BotMetadata white,
        fr.astroware.chess.bot.api.BotMetadata black,
        PositionView initialPosition
    ) {
        out.println("============================================================");
        out.println("                    CHESS FRAMEWORK");
        out.println("============================================================");
        out.printf("Blancs : %s — %s%n", white.botName(), white.authorName());
        out.printf("Noirs  : %s — %s%n", black.botName(), black.authorName());
        out.println("------------------------------------------------------------");
        out.println("Début de la partie");
        out.printf("FEN : %s%n%n", initialPosition.fen());
    }

    @Override
    public void onMovePlayed(PlayedMove move) {
        String prefix = move.color() == Color.WHITE
            ? move.fullMoveNumber() + "."
            : move.fullMoveNumber() + "...";

        RuleAttempt selected = move.decision().trace().stream()
            .filter(attempt -> attempt.status() == AttemptStatus.SELECTED)
            .findFirst()
            .orElse(null);

        out.printf(
            "%-6s %-18s %-8s",
            prefix,
            move.bot().botName(),
            move.san()
        );

        if (selected != null) {
            out.printf("  [%s]", selected.ruleName());

            selected.selectedMove().ifPresent(candidate ->
                out.printf(
                    " score=%.2f/10 risque=%.1f sécurité=%.1f temps=%.1fms",
                    candidate.score().value(),
                    candidate.risk().value(),
                    candidate.safety().value(),
                    move.decisionMillis()
                )
            );
        }

        out.println();

        if (verbose && selected != null && selected.candidates().size() > 1) {
            selected.candidates().stream()
                .sorted(
                    Comparator.comparingDouble(
                        candidate -> -candidate.score().value()
                    )
                )
                .forEach(candidate ->
                    out.printf(
                        "       candidat %-7s score=%4.1f  aggr=%4.1f  sec=%4.1f  risque=%4.1f  %s%n",
                        candidate.move().toUci(),
                        candidate.score().value(),
                        candidate.aggression().value(),
                        candidate.safety().value(),
                        candidate.risk().value(),
                        candidate.explanation()
                    )
                );
        }
    }

    @Override
    public void onMatchEnded(MatchResult result) {
        out.println();
        out.println("------------------------------------------------------------");
        out.println("Fin de la partie");
        out.printf("Résultat : %s%n", humanResult(result));
        out.printf("PGN      : %s%n", result.pgnResult());
        out.printf("Demi-coups : %d%n", result.pliesPlayed());
        out.printf("Coups       : %d%n", result.fullMovesPlayed());
        out.printf("Terminaison : %s%n", result.termination());

        result.incident().ifPresent(incident ->
            out.printf(
                "Incident    : %s%n",
                incident.summary()
            )
        );
        out.printf(
            "Temps moyen Blancs : %.1f ms (max %.1f ms)%n",
            result.averageDecisionMillis(Color.WHITE),
            result.maxDecisionMillis(Color.WHITE)
        );
        out.printf(
            "Temps moyen Noirs  : %.1f ms (max %.1f ms)%n",
            result.averageDecisionMillis(Color.BLACK),
            result.maxDecisionMillis(Color.BLACK)
        );
        out.printf("FEN finale  : %s%n", result.finalFen());
        out.println("============================================================");
    }

    private static String humanResult(MatchResult result) {
        if (result.gameResult().isEmpty()) {
            return "partie interrompue par la limite technique";
        }

        return switch (result.gameResult().orElseThrow().status()) {
            case WHITE_WINS ->
                result.white().botName() + " gagne avec les Blancs";
            case BLACK_WINS ->
                result.black().botName() + " gagne avec les Noirs";
            case DRAW -> "partie nulle"
                + result.gameResult()
                    .orElseThrow()
                    .drawReason()
                    .map(reason -> " (" + reason + ")")
                    .orElse("");
            case ONGOING -> "partie en cours";
        };
    }
}
