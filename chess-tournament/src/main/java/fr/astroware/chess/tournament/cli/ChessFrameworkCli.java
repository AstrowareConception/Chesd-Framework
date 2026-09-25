package fr.astroware.chess.tournament.cli;

import fr.astroware.chess.bot.api.BotMetadata;
import fr.astroware.chess.bot.api.ChessBot;
import fr.astroware.chess.tournament.console.ConsoleMatchListener;
import fr.astroware.chess.tournament.console.ConsoleTournamentReporter;
import fr.astroware.chess.tournament.execution.BotPlayer;
import fr.astroware.chess.tournament.execution.BotPlayerFactory;
import fr.astroware.chess.tournament.execution.BotPlayers;
import fr.astroware.chess.tournament.execution.IsolatedBotPlayerFactory;
import fr.astroware.chess.tournament.execution.IsolatedBotSettings;
import fr.astroware.chess.tournament.match.BotFactory;
import fr.astroware.chess.tournament.match.MatchConfiguration;
import fr.astroware.chess.tournament.match.MatchResult;
import fr.astroware.chess.tournament.match.MatchRunner;
import fr.astroware.chess.tournament.pgn.PgnExporter;
import fr.astroware.chess.tournament.roundrobin.RoundRobinConfiguration;
import fr.astroware.chess.tournament.roundrobin.RoundRobinPgnExporter;
import fr.astroware.chess.tournament.roundrobin.RoundRobinResult;
import fr.astroware.chess.tournament.roundrobin.RoundRobinTournament;
import fr.astroware.chess.tournament.roundrobin.StandingsCsvExporter;
import fr.astroware.chess.tournament.roundrobin.TournamentParticipant;
import fr.astroware.chess.tournament.submission.StudentSubmissionValidator;
import fr.astroware.chess.tournament.ui.SwingMatchViewer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * CLI du framework de tournoi.
 */
public final class ChessFrameworkCli {

    private static final long DEFAULT_SEED = 42L;
    private static final int DEFAULT_MAX_PLIES = 400;
    private static final int DEFAULT_GAMES_PER_PAIR = 2;
    private static final int DEFAULT_TIMEOUT_MS = 2_000;
    private static final int DEFAULT_STARTUP_TIMEOUT_MS = 5_000;
    private static final int DEFAULT_HEAP_MB = 256;

    private ChessFrameworkCli() {
    }

    public static void main(String[] args) throws IOException {
        if (args.length == 0
            || "help".equalsIgnoreCase(args[0])) {
            printUsage();
            return;
        }

        if ("list".equalsIgnoreCase(args[0])) {
            printBots();
            return;
        }

        String mode =
            args[0].toLowerCase(Locale.ROOT);

        if ("tournament".equals(mode)) {
            runTournament(args);
            return;
        }

        if ("validate-students".equals(mode)) {
            validateStudents();
            return;
        }

        if (args.length < 3) {
            printUsage();
            return;
        }

        runDuel(mode, args);
    }

    private static void validateStudents() {
        var report =
            new StudentSubmissionValidator()
                .validate();

        if (report.validatedBots().isEmpty()) {
            System.out.println(
                "Aucun bot étudiant compilé à valider."
            );
            return;
        }

        System.out.println(
            "Bots étudiants validés :"
        );

        report.validatedBots().forEach(bot ->
            System.out.printf(
                "  - %s — %s (%s)%n",
                bot.metadata().botName(),
                bot.metadata().authorName(),
                bot.className()
            )
        );
    }

    private static void runDuel(
        String mode,
        String[] args
    ) throws IOException {
        boolean isolated =
            hasFlag(args, "--isolated");

        IsolatedBotSettings settings =
            readIsolationSettings(args);

        BotPlayerFactory<? extends BotPlayer> white =
            playerFactory(
                args[1],
                isolated,
                settings
            );

        BotPlayerFactory<? extends BotPlayer> black =
            playerFactory(
                args[2],
                isolated,
                settings
            );

        MatchConfiguration configuration =
            new MatchConfiguration(
                readMaxPlies(args),
                readSeed(args),
                java.util.Optional.empty()
            );

        if (isolated) {
            printIsolationSettings(settings);
        }

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
                    runner.play(
                        white,
                        black,
                        configuration
                    );

                String pgn =
                    new PgnExporter().export(result);

                Path output =
                    readPgnOutput(args);

                if (output != null) {
                    Files.writeString(
                        output,
                        pgn,
                        StandardCharsets.UTF_8
                    );

                    System.out.println(
                        "PGN écrit dans : "
                            + output.toAbsolutePath()
                    );
                } else {
                    System.out.println(pgn);
                }

                printSummary(result);
            }

            case "gui" -> {
                MatchResult result =
                    runner.play(
                        white,
                        black,
                        configuration
                    );

                printSummary(result);
                SwingMatchViewer.show(result);
            }

            default -> {
                System.err.println(
                    "Mode inconnu : " + mode
                );
                printUsage();
            }
        }
    }

    private static void runTournament(
        String[] args
    ) {
        boolean isolated =
            hasFlag(args, "--isolated");

        IsolatedBotSettings isolationSettings =
            readIsolationSettings(args);

        List<String> requestedBots =
            tournamentBotNames(args);

        boolean all =
            hasFlag(args, "--all");

        List<String> botNames;

        if (all) {
            botNames = new ArrayList<>(
                BotCatalog.all().keySet()
            );
            botNames.sort(String::compareTo);
        } else {
            if (requestedBots.size() < 2) {
                throw new IllegalArgumentException(
                    "Le tournoi nécessite au moins deux bots "
                        + "ou l'option --all"
                );
            }

            botNames = List.copyOf(
                requestedBots
            );
        }

        List<TournamentParticipant> participants =
            botNames.stream()
                .map(name ->
                    tournamentParticipant(
                        name,
                        isolated,
                        isolationSettings
                    )
                )
                .toList();

        if (isolated) {
            printIsolationSettings(
                isolationSettings
            );
        }

        RoundRobinResult result =
            new RoundRobinTournament().play(
                participants,
                new RoundRobinConfiguration(
                    readGamesPerPair(args),
                    readMaxPlies(args),
                    readSeed(args)
                )
            );

        new ConsoleTournamentReporter()
            .print(result);

        writeTournamentExports(
            args,
            result
        );
    }

    private static TournamentParticipant
        tournamentParticipant(
            String name,
            boolean isolated,
            IsolatedBotSettings settings
        ) {

        String key =
            name.toLowerCase(Locale.ROOT);

        BotFactory factory =
            requireBot(name);

        BotMetadata metadata =
            factory.create().metadata();

        if (!isolated) {
            return new TournamentParticipant(
                key,
                factory
            );
        }

        return TournamentParticipant.isolated(
            key,
            requireBotClass(name),
            metadata,
            settings
        );
    }

    private static BotPlayerFactory<? extends BotPlayer>
        playerFactory(
            String name,
            boolean isolated,
            IsolatedBotSettings settings
        ) {

        if (isolated) {
            return new IsolatedBotPlayerFactory(
                requireBotClass(name),
                settings
            );
        }

        return BotPlayers.inProcess(
            requireBot(name)
        );
    }

    private static void writeTournamentExports(
        String[] args,
        RoundRobinResult result
    ) {
        for (String arg : args) {
            if (arg.startsWith("--pgn=")) {
                Path path = Path.of(
                    arg.substring(
                        "--pgn=".length()
                    )
                );

                try {
                    Files.writeString(
                        path,
                        new RoundRobinPgnExporter()
                            .export(result),
                        StandardCharsets.UTF_8
                    );
                } catch (IOException exception) {
                    throw new IllegalStateException(
                        "Impossible d'écrire le PGN du tournoi : "
                            + path,
                        exception
                    );
                }

                System.out.println(
                    "PGN tournoi : "
                        + path.toAbsolutePath()
                );
            }

            if (arg.startsWith("--csv=")) {
                Path path = Path.of(
                    arg.substring(
                        "--csv=".length()
                    )
                );

                try {
                    Files.writeString(
                        path,
                        new StandingsCsvExporter()
                            .export(result),
                        StandardCharsets.UTF_8
                    );
                } catch (IOException exception) {
                    throw new IllegalStateException(
                        "Impossible d'écrire le CSV du classement : "
                            + path,
                        exception
                    );
                }

                System.out.println(
                    "CSV classement : "
                        + path.toAbsolutePath()
                );
            }
        }
    }

    private static BotFactory requireBot(
        String name
    ) {
        return BotCatalog.find(name)
            .orElseThrow(() ->
                unknownBot(name)
            );
    }

    private static Class<? extends ChessBot>
        requireBotClass(String name) {

        return BotCatalog.findClass(name)
            .orElseThrow(() ->
                unknownBot(name)
            );
    }

    private static IllegalArgumentException
        unknownBot(String name) {

        return new IllegalArgumentException(
            "Bot inconnu : "
                + name
                + ". Utilisez 'list' pour voir les bots."
        );
    }

    private static List<String> tournamentBotNames(
        String[] args
    ) {
        List<String> names =
            new ArrayList<>();

        for (int index = 1;
            index < args.length;
            index++) {

            String arg = args[index];

            if (!arg.startsWith("--")) {
                names.add(arg);
            }
        }

        return names;
    }

    private static boolean hasFlag(
        String[] args,
        String flag
    ) {
        return java.util.Arrays.stream(args)
            .anyMatch(
                value ->
                    flag.equalsIgnoreCase(value)
            );
    }

    private static IsolatedBotSettings
        readIsolationSettings(
            String[] args
        ) {

        return new IsolatedBotSettings(
            Duration.ofMillis(
                readPositiveIntOption(
                    args,
                    "--startup-timeout-ms=",
                    DEFAULT_STARTUP_TIMEOUT_MS
                )
            ),
            Duration.ofMillis(
                readPositiveIntOption(
                    args,
                    "--timeout-ms=",
                    DEFAULT_TIMEOUT_MS
                )
            ),
            readPositiveIntOption(
                args,
                "--heap-mb=",
                DEFAULT_HEAP_MB
            )
        );
    }

    private static long readSeed(
        String[] args
    ) {
        for (String arg : args) {
            if (arg.startsWith("--seed=")) {
                return Long.parseLong(
                    arg.substring(
                        "--seed=".length()
                    )
                );
            }
        }

        return DEFAULT_SEED;
    }

    private static int readGamesPerPair(
        String[] args
    ) {
        return readPositiveIntOption(
            args,
            "--games=",
            DEFAULT_GAMES_PER_PAIR
        );
    }

    private static int readMaxPlies(
        String[] args
    ) {
        return readPositiveIntOption(
            args,
            "--max-plies=",
            DEFAULT_MAX_PLIES
        );
    }

    private static int readPositiveIntOption(
        String[] args,
        String prefix,
        int defaultValue
    ) {
        for (String arg : args) {
            if (arg.startsWith(prefix)) {
                int value = Integer.parseInt(
                    arg.substring(
                        prefix.length()
                    )
                );

                if (value <= 0) {
                    throw new IllegalArgumentException(
                        prefix
                            + " doit être strictement positif"
                    );
                }

                return value;
            }
        }

        return defaultValue;
    }

    private static Path readPgnOutput(
        String[] args
    ) {
        for (
            int index = 3;
            index < args.length;
            index++
        ) {
            String arg = args[index];

            if (!arg.startsWith("--")) {
                return Path.of(arg);
            }
        }

        return null;
    }

    private static void printIsolationSettings(
        IsolatedBotSettings settings
    ) {
        System.out.printf(
            Locale.ROOT,
            "Isolation JVM : timeout=%d ms, démarrage=%d ms, heap=%d MiB%n",
            settings.decisionTimeoutMillis(),
            settings.startupTimeoutMillis(),
            settings.maxHeapMegabytes()
        );
    }

    private static void printSummary(
        MatchResult result
    ) {
        System.out.println();
        System.out.println(
            result.white().botName()
                + " vs "
                + result.black().botName()
        );
        System.out.println(
            "Résultat : "
                + result.pgnResult()
        );
        System.out.println(
            "Coups : "
                + result.fullMovesPlayed()
                + " ("
                + result.pliesPlayed()
                + " demi-coups)"
        );
        System.out.println(
            "Fin : "
                + result.termination()
        );

        result.incident().ifPresent(
            incident ->
                System.out.println(
                    "Incident : "
                        + incident.summary()
                )
        );
    }

    private static void printBots() {
        System.out.println(
            "Bots disponibles :"
        );

        for (
            Map.Entry<String, BotFactory> entry
            : BotCatalog.all().entrySet()
        ) {
            var metadata =
                entry.getValue()
                    .create()
                    .metadata();

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
              validate-students
              console <blancs> <noirs> [options]
              pgn     <blancs> <noirs> [fichier.pgn] [options]
              gui     <blancs> <noirs> [options]
              tournament <bot1> <bot2> [...] [options]
              tournament --all [options]

            Options communes :
              --seed=N
              --max-plies=N

            Isolation JVM :
              --isolated
              --timeout-ms=N
              --startup-timeout-ms=N
              --heap-mb=N

            Options tournoi :
              --games=N
              --pgn=parties.pgn
              --csv=classement.csv

            Exemples :
              console tactical random
              console minimax random --isolated
              console minimax random --isolated --timeout-ms=3000 --heap-mb=256
              pgn tactical guardian partie.pgn --isolated
              gui architect tactical --isolated
              tournament random greedy tactical
              tournament positional lookahead minimax --games=2 --isolated
              tournament --all --games=2 --isolated --timeout-ms=3000
              tournament tactical positional --pgn=parties.pgn --csv=classement.csv

            Utilisez :
              list

            pour afficher les bots disponibles.
            """
        );
    }
}
