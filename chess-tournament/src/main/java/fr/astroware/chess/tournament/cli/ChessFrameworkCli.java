package fr.astroware.chess.tournament.cli;

import fr.astroware.chess.tournament.console.ConsoleMatchListener;
import fr.astroware.chess.tournament.match.BotFactory;
import fr.astroware.chess.tournament.match.MatchConfiguration;
import fr.astroware.chess.tournament.match.MatchResult;
import fr.astroware.chess.tournament.match.MatchRunner;
import fr.astroware.chess.tournament.pgn.PgnExporter;
import fr.astroware.chess.tournament.ui.SwingMatchViewer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;

/**
 * Point d'entrée simple pour lancer un duel entre bots.
 *
 * <p>Exemples :</p>
 *
 * <pre>
 * console tactical random
 * pgn tactical guardian partie.pgn
 * gui architect berserker
 * list
 * </pre>
 */
public final class ChessFrameworkCli {

    private static final long DEFAULT_SEED = 42L;

    private ChessFrameworkCli() {
    }

    public static void main(String[] args) throws IOException {
        if (args.length == 0 || "help".equalsIgnoreCase(args[0])) {
            printUsage();
            return;
        }

        if ("list".equalsIgnoreCase(args[0])) {
            printBots();
            return;
        }

        if (args.length < 3) {
            printUsage();
            return;
        }

        String mode = args[0].toLowerCase(Locale.ROOT);
        BotFactory white = requireBot(args[1]);
        BotFactory black = requireBot(args[2]);

        long seed = readSeed(args);
        int maxPlies = readMaxPlies(args);

        MatchConfiguration configuration = new MatchConfiguration(
            maxPlies,
            seed,
            java.util.Optional.empty()
        );

        MatchRunner runner = new MatchRunner();

        switch (mode) {
            case "console" -> runner.play(
                white,
                black,
                configuration,
                new ConsoleMatchListener()
            );

            case "pgn" -> {
                MatchResult result =
                    runner.play(white, black, configuration);

                String pgn = new PgnExporter().export(result);

                if (args.length >= 4
                    && !args[3].startsWith("--")) {
                    Path path = Path.of(args[3]);
                    Files.writeString(
                        path,
                        pgn,
                        StandardCharsets.UTF_8
                    );

                    System.out.println(
                        "PGN écrit dans : "
                            + path.toAbsolutePath()
                    );
                } else {
                    System.out.println(pgn);
                }

                printSummary(result);
            }

            case "gui" -> {
                MatchResult result =
                    runner.play(white, black, configuration);

                printSummary(result);
                SwingMatchViewer.show(result);
            }

            default -> {
                System.err.println("Mode inconnu : " + mode);
                printUsage();
            }
        }
    }

    private static BotFactory requireBot(String name) {
        return BotCatalog.find(name)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Bot inconnu : "
                        + name
                        + ". Utilisez 'list' pour voir les bots."
                )
            );
    }

    private static long readSeed(String[] args) {
        for (String arg : args) {
            if (arg.startsWith("--seed=")) {
                return Long.parseLong(
                    arg.substring("--seed=".length())
                );
            }
        }

        return DEFAULT_SEED;
    }

    private static int readMaxPlies(String[] args) {
        for (String arg : args) {
            if (arg.startsWith("--max-plies=")) {
                return Integer.parseInt(
                    arg.substring("--max-plies=".length())
                );
            }
        }

        return 400;
    }

    private static void printSummary(MatchResult result) {
        System.out.println();
        System.out.println(
            result.white().botName()
                + " vs "
                + result.black().botName()
        );
        System.out.println("Résultat : " + result.pgnResult());
        System.out.println(
            "Coups : "
                + result.fullMovesPlayed()
                + " ("
                + result.pliesPlayed()
                + " demi-coups)"
        );
        System.out.println("Fin : " + result.termination());
    }

    private static void printBots() {
        System.out.println("Bots disponibles :");

        for (Map.Entry<String, BotFactory> entry
            : BotCatalog.all().entrySet()) {

            var metadata = entry.getValue().create().metadata();

            System.out.printf(
                "  %-12s %-20s — %s%n",
                entry.getKey(),
                metadata.botName(),
                metadata.description()
            );
        }
    }

    private static void printUsage() {
        System.out.println(
            """
            Chess Framework

            Usage :
              list
              console <blancs> <noirs> [--seed=N] [--max-plies=N]
              pgn     <blancs> <noirs> [fichier.pgn] [--seed=N] [--max-plies=N]
              gui     <blancs> <noirs> [--seed=N] [--max-plies=N]

            Exemples :
              console tactical random
              console cautious berserker --seed=123
              pgn tactical guardian partie.pgn
              gui architect tactical

            Utilisez :
              list

            pour afficher les bots disponibles.
            """
        );
    }
}
