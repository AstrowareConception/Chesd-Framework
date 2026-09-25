package fr.astroware.chess.tournament.submission;

import fr.astroware.chess.bot.api.BotMetadata;
import fr.astroware.chess.bot.api.ChessBot;
import fr.astroware.chess.bots.baseline.RandomBot;
import fr.astroware.chess.tournament.cli.BotCatalog;
import fr.astroware.chess.tournament.execution.BotPlayers;
import fr.astroware.chess.tournament.execution.IsolatedBotPlayerFactory;
import fr.astroware.chess.tournament.execution.IsolatedBotSettings;
import fr.astroware.chess.tournament.match.MatchConfiguration;
import fr.astroware.chess.tournament.match.MatchIncident;
import fr.astroware.chess.tournament.match.MatchResult;
import fr.astroware.chess.tournament.match.MatchRunner;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Validation exécutable des bots présents dans le package students.
 *
 * <p>Chaque bot est chargé dans une JVM isolée et doit réussir un court test
 * comme Blanc puis comme Noir. Le constructeur, metadata() et decide() sont
 * donc couverts par les mêmes protections que le tournoi final.</p>
 */
public final class StudentSubmissionValidator {

    private static final String STUDENT_PACKAGE =
        "fr.astroware.chess.bots.students";

    private static final IsolatedBotSettings SETTINGS =
        new IsolatedBotSettings(
            Duration.ofSeconds(5),
            Duration.ofSeconds(2),
            128
        );

    public ValidationReport validate() {
        List<Class<? extends ChessBot>> studentBots =
            discoverStudentBots();

        if (studentBots.isEmpty()) {
            return new ValidationReport(
                List.of(),
                List.of()
            );
        }

        Set<String> reservedNames =
            referenceBotNames();

        Set<String> studentNames =
            new HashSet<>();

        List<ValidatedBot> validated =
            new ArrayList<>();

        List<String> errors =
            new ArrayList<>();

        for (Class<? extends ChessBot> botClass
            : studentBots) {

            try {
                validateClassShape(botClass);

                BotMetadata metadata =
                    smokeTest(botClass);

                validateMetadata(
                    botClass,
                    metadata,
                    reservedNames,
                    studentNames
                );

                validated.add(
                    new ValidatedBot(
                        botClass.getName(),
                        metadata
                    )
                );
            } catch (RuntimeException exception) {
                errors.add(
                    botClass.getName()
                        + " : "
                        + compactMessage(exception)
                );
            }
        }

        ValidationReport report =
            new ValidationReport(
                validated,
                errors
            );

        report.throwIfInvalid();
        return report;
    }

    private static void validateClassShape(
        Class<? extends ChessBot> botClass
    ) {
        int modifiers =
            botClass.getModifiers();

        if (!Modifier.isPublic(modifiers)) {
            throw new IllegalStateException(
                "la classe doit être public"
            );
        }

        if (Modifier.isAbstract(modifiers)) {
            throw new IllegalStateException(
                "la classe ne doit pas être abstract"
            );
        }

        try {
            botClass.getConstructor();
        } catch (NoSuchMethodException exception) {
            throw new IllegalStateException(
                "un constructeur public sans argument est requis",
                exception
            );
        }
    }

    private static BotMetadata smokeTest(
        Class<? extends ChessBot> botClass
    ) {
        IsolatedBotPlayerFactory isolated =
            new IsolatedBotPlayerFactory(
                botClass,
                SETTINGS
            );

        MatchRunner runner =
            new MatchRunner();

        MatchResult asWhite =
            runner.play(
                isolated,
                BotPlayers.inProcess(
                    RandomBot::new
                ),
                new MatchConfiguration(
                    2,
                    0x51A7E001L,
                    java.util.Optional.empty()
                )
            );

        assertStudentDidNotForfeit(
            asWhite,
            true
        );

        MatchResult asBlack =
            runner.play(
                BotPlayers.inProcess(
                    RandomBot::new
                ),
                isolated,
                new MatchConfiguration(
                    2,
                    0xB1AC0001L,
                    java.util.Optional.empty()
                )
            );

        assertStudentDidNotForfeit(
            asBlack,
            false
        );

        return asWhite.white();
    }

    private static void assertStudentDidNotForfeit(
        MatchResult result,
        boolean studentIsWhite
    ) {
        if (!result.isForfeit()) {
            return;
        }

        MatchIncident incident =
            result.incident().orElseThrow();

        boolean studentOffender =
            studentIsWhite
                ? incident.offenderColor()
                    == fr.astroware.chess.core.model.Color.WHITE
                : incident.offenderColor()
                    == fr.astroware.chess.core.model.Color.BLACK;

        if (studentOffender) {
            throw new IllegalStateException(
                "échec du smoke test isolé : "
                    + incident.summary()
            );
        }
    }

    private static void validateMetadata(
        Class<? extends ChessBot> botClass,
        BotMetadata metadata,
        Set<String> reservedNames,
        Set<String> studentNames
    ) {
        String botName =
            metadata.botName().trim();

        String author =
            metadata.authorName().trim();

        String description =
            metadata.description().trim();

        if (botName.isEmpty()) {
            throw new IllegalStateException(
                "botName est vide"
            );
        }

        if (author.isEmpty()) {
            throw new IllegalStateException(
                "authorName est vide"
            );
        }

        if (description.isEmpty()) {
            throw new IllegalStateException(
                "description est vide"
            );
        }

        String normalized =
            botName.toLowerCase(Locale.ROOT);

        if (reservedNames.contains(normalized)) {
            throw new IllegalStateException(
                "le nom de bot '"
                    + botName
                    + "' est déjà utilisé par un bot de référence"
            );
        }

        if (!studentNames.add(normalized)) {
            throw new IllegalStateException(
                "le nom de bot '"
                    + botName
                    + "' est déjà utilisé par une autre soumission"
            );
        }

        if (!botClass.getPackageName()
            .equals(STUDENT_PACKAGE)) {
            throw new IllegalStateException(
                "package invalide : "
                    + botClass.getPackageName()
            );
        }
    }

    private static Set<String> referenceBotNames() {
        Set<String> names =
            new HashSet<>();

        BotCatalog.all().values()
            .forEach(factory ->
                names.add(
                    factory.create()
                        .metadata()
                        .botName()
                        .trim()
                        .toLowerCase(Locale.ROOT)
                )
            );

        return names;
    }

    private static List<Class<? extends ChessBot>>
        discoverStudentBots() {

        Path packageDirectory =
            studentPackageDirectory();

        if (!Files.isDirectory(
            packageDirectory
        )) {
            return List.of();
        }

        List<Class<? extends ChessBot>> bots =
            new ArrayList<>();

        try (var paths = Files.walk(packageDirectory)) {
            paths
                .filter(Files::isRegularFile)
                .filter(path ->
                    path.getFileName()
                        .toString()
                        .endsWith(".class")
                )
                .filter(path ->
                    !path.getFileName()
                        .toString()
                        .contains("$")
                )
                .forEach(path ->
                    loadStudentBot(
                        packageDirectory,
                        path
                    ).ifPresent(bots::add)
                );
        } catch (IOException exception) {
            throw new IllegalStateException(
                "Impossible de parcourir les classes students",
                exception
            );
        }

        bots.sort(
            java.util.Comparator.comparing(
                Class::getName
            )
        );

        return List.copyOf(bots);
    }

    private static java.util.Optional<
        Class<? extends ChessBot>>
        loadStudentBot(
            Path packageDirectory,
            Path classFile
        ) {

        Path relative =
            packageDirectory.relativize(
                classFile
            );

        String suffix =
            relative.toString()
                .replace(
                    java.io.File.separatorChar,
                    '.'
                )
                .replaceAll(
                    "\\.class$",
                    ""
                );

        String className =
            STUDENT_PACKAGE
                + (suffix.isBlank()
                    ? ""
                    : "." + suffix);

        try {
            Class<?> raw =
                Class.forName(className);

            if (!ChessBot.class
                .isAssignableFrom(raw)) {
                return java.util.Optional.empty();
            }

            @SuppressWarnings("unchecked")
            Class<? extends ChessBot> botClass =
                (Class<? extends ChessBot>) raw;

            return java.util.Optional.of(
                botClass
            );
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException(
                "Classe introuvable : "
                    + className,
                exception
            );
        }
    }

    private static Path studentPackageDirectory() {
        try {
            URI root =
                RandomBot.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI();

            return Path.of(root)
                .resolve(
                    STUDENT_PACKAGE.replace(
                        '.',
                        java.io.File.separatorChar
                    )
                );
        } catch (Exception exception) {
            throw new IllegalStateException(
                "Impossible de localiser chess-bots/target/classes",
                exception
            );
        }
    }

    private static String compactMessage(
        RuntimeException exception
    ) {
        String message =
            exception.getMessage();

        return message == null
            || message.isBlank()
            ? exception.getClass().getSimpleName()
            : message.replaceAll("\\s+", " ")
                .trim();
    }

    public record ValidatedBot(
        String className,
        BotMetadata metadata
    ) {
    }

    public record ValidationReport(
        List<ValidatedBot> validatedBots,
        List<String> errors
    ) {
        public ValidationReport {
            validatedBots =
                List.copyOf(validatedBots);
            errors = List.copyOf(errors);
        }

        public boolean valid() {
            return errors.isEmpty();
        }

        public void throwIfInvalid() {
            if (valid()) {
                return;
            }

            throw new IllegalStateException(
                "Soumission étudiante invalide :\n - "
                    + String.join(
                        "\n - ",
                        errors
                    )
            );
        }
    }
}
