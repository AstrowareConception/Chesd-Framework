package fr.astroware.chess.tournament.submission;

import fr.astroware.chess.bot.api.ChessBot;
import fr.astroware.chess.bots.baseline.RandomBot;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;

/**
 * Découvre les ChessBot compilés dans le package étudiant.
 *
 * <p>La découverte fonctionne depuis un répertoire Maven target/classes
 * comme depuis un JAR installé.</p>
 */
public final class StudentBotDiscovery {

    public static final String STUDENT_PACKAGE =
        "fr.astroware.chess.bots.students";

    private StudentBotDiscovery() {
    }

    public static List<Class<? extends ChessBot>> discover() {
        Set<String> classNames =
            new LinkedHashSet<>();

        for (Path entry : classPathEntries()) {
            if (Files.isDirectory(entry)) {
                collectFromDirectory(
                    entry,
                    classNames
                );
            } else if (Files.isRegularFile(entry)
                && entry.getFileName()
                    .toString()
                    .endsWith(".jar")) {

                collectFromJar(
                    entry,
                    classNames
                );
            }
        }

        List<Class<? extends ChessBot>> bots =
            classNames.stream()
                .map(StudentBotDiscovery::loadStudentBot)
                .flatMap(java.util.Optional::stream)
                .sorted(
                    java.util.Comparator.comparing(
                        Class::getName
                    )
                )
                .toList();

        return List.copyOf(bots);
    }

    private static List<Path> classPathEntries() {
        Set<Path> entries =
            new LinkedHashSet<>();

        addClassPathProperty(
            entries,
            System.getProperty(
                "surefire.test.class.path",
                ""
            )
        );

        addClassPathProperty(
            entries,
            System.getProperty(
                "java.class.path",
                ""
            )
        );

        try {
            URI codeSource =
                RandomBot.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI();

            entries.add(Path.of(codeSource));
        } catch (Exception exception) {
            throw new IllegalStateException(
                "Impossible de localiser le module chess-bots",
                exception
            );
        }

        return List.copyOf(entries);
    }

    private static void addClassPathProperty(
        Set<Path> entries,
        String classPath
    ) {
        if (classPath == null
            || classPath.isBlank()) {
            return;
        }

        for (String raw
            : classPath.split(
                java.util.regex.Pattern.quote(
                    java.io.File.pathSeparator
                )
            )) {

            if (!raw.isBlank()) {
                entries.add(Path.of(raw));
            }
        }
    }

    private static void collectFromDirectory(
        Path classPathRoot,
        Set<String> classNames
    ) {
        Path packageDirectory =
            classPathRoot.resolve(
                STUDENT_PACKAGE.replace(
                    '.',
                    java.io.File.separatorChar
                )
            );

        if (!Files.isDirectory(packageDirectory)) {
            return;
        }

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
                .forEach(path -> {
                    Path relative =
                        packageDirectory.relativize(path);

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

                    classNames.add(
                        STUDENT_PACKAGE
                            + "."
                            + suffix
                    );
                });
        } catch (IOException exception) {
            throw new IllegalStateException(
                "Impossible de parcourir "
                    + packageDirectory,
                exception
            );
        }
    }

    private static void collectFromJar(
        Path jarPath,
        Set<String> classNames
    ) {
        String prefix =
            STUDENT_PACKAGE.replace('.', '/')
                + "/";

        try (JarFile jar = new JarFile(
            jarPath.toFile()
        )) {
            jar.stream()
                .map(java.util.jar.JarEntry::getName)
                .filter(name ->
                    name.startsWith(prefix)
                )
                .filter(name ->
                    name.endsWith(".class")
                )
                .filter(name ->
                    !name.contains("$")
                )
                .forEach(name ->
                    classNames.add(
                        name.substring(
                            0,
                            name.length()
                                - ".class".length()
                        ).replace('/', '.')
                    )
                );
        } catch (IOException exception) {
            throw new IllegalStateException(
                "Impossible de parcourir le JAR "
                    + jarPath,
                exception
            );
        }
    }

    private static java.util.Optional<
        Class<? extends ChessBot>>
        loadStudentBot(
            String className
        ) {

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
}
