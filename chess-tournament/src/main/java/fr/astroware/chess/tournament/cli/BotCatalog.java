package fr.astroware.chess.tournament.cli;

import fr.astroware.chess.bot.api.ChessBot;
import fr.astroware.chess.bots.baseline.BerserkerBot;
import fr.astroware.chess.bots.baseline.CautiousBot;
import fr.astroware.chess.bots.baseline.ChameleonBot;
import fr.astroware.chess.bots.baseline.GreedyBot;
import fr.astroware.chess.bots.baseline.GuardianBot;
import fr.astroware.chess.bots.baseline.LookaheadBot;
import fr.astroware.chess.bots.baseline.MinimaxBot;
import fr.astroware.chess.bots.baseline.PressureBot;
import fr.astroware.chess.bots.baseline.PositionalBot;
import fr.astroware.chess.bots.baseline.RandomBot;
import fr.astroware.chess.bots.baseline.TacticalBot;
import fr.astroware.chess.bots.examples.SolidPlannerBot;
import fr.astroware.chess.tournament.match.BotFactory;
import fr.astroware.chess.tournament.submission.StudentBotDiscovery;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Catalogue des bots disponibles pour le CLI et le tournoi.
 *
 * <p>Les bots de référence sont enregistrés explicitement. Les bots étudiants
 * compilés dans {@code fr.astroware.chess.bots.students} sont découverts
 * automatiquement et ajoutés avec une clé préfixée par {@code student-}.</p>
 */
public final class BotCatalog {

    private static final Map<String, Registration>
        REFERENCE_REGISTRATIONS =
            createReferenceCatalog();

    private static final Map<String, Registration>
        REGISTRATIONS =
            createCombinedCatalog();

    private BotCatalog() {
    }

    /**
     * Tous les bots visibles par le tournoi : références + étudiants.
     */
    public static Map<String, BotFactory> all() {
        return factories(REGISTRATIONS);
    }

    /**
     * Bots livrés par le framework uniquement.
     *
     * <p>Cette vue est notamment utilisée pour vérifier qu'un étudiant ne
     * réutilise pas le nom d'un bot de référence.</p>
     */
    public static Map<String, BotFactory> referenceBots() {
        return factories(
            REFERENCE_REGISTRATIONS
        );
    }

    public static Optional<BotFactory> find(
        String name
    ) {
        return registration(name)
            .map(Registration::factory);
    }

    public static Optional<Class<? extends ChessBot>>
        findClass(String name) {

        return registration(name)
            .map(Registration::botClass);
    }

    private static Optional<Registration> registration(
        String name
    ) {
        if (name == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(
            REGISTRATIONS.get(
                normalizeKey(name)
            )
        );
    }

    private static Map<String, BotFactory> factories(
        Map<String, Registration> registrations
    ) {
        Map<String, BotFactory> factories =
            new LinkedHashMap<>();

        registrations.forEach(
            (key, registration) ->
                factories.put(
                    key,
                    registration.factory()
                )
        );

        return Map.copyOf(factories);
    }

    private static Map<String, Registration>
        createCombinedCatalog() {

        Map<String, Registration> bots =
            new LinkedHashMap<>(
                REFERENCE_REGISTRATIONS
            );

        for (Class<? extends ChessBot> botClass
            : StudentBotDiscovery.discover()) {

            String key =
                studentKey(botClass);

            if (bots.containsKey(key)) {
                throw new IllegalStateException(
                    "Clé de bot dupliquée : "
                        + key
                );
            }

            bots.put(
                key,
                new Registration(
                    botClass,
                    reflectiveFactory(botClass)
                )
            );
        }

        return Map.copyOf(bots);
    }

    private static Map<String, Registration>
        createReferenceCatalog() {

        Map<String, Registration> bots =
            new LinkedHashMap<>();

        register(
            bots,
            "random",
            RandomBot.class,
            RandomBot::new
        );
        register(
            bots,
            "greedy",
            GreedyBot.class,
            GreedyBot::new
        );
        register(
            bots,
            "cautious",
            CautiousBot.class,
            CautiousBot::new
        );
        register(
            bots,
            "chameleon",
            ChameleonBot.class,
            ChameleonBot::new
        );
        register(
            bots,
            "berserker",
            BerserkerBot.class,
            BerserkerBot::new
        );
        register(
            bots,
            "guardian",
            GuardianBot.class,
            GuardianBot::new
        );
        register(
            bots,
            "lookahead",
            LookaheadBot.class,
            LookaheadBot::new
        );
        register(
            bots,
            "minimax",
            MinimaxBot.class,
            MinimaxBot::new
        );
        register(
            bots,
            "pressure",
            PressureBot.class,
            PressureBot::new
        );
        register(
            bots,
            "positional",
            PositionalBot.class,
            PositionalBot::new
        );
        register(
            bots,
            "tactical",
            TacticalBot.class,
            TacticalBot::new
        );
        register(
            bots,
            "architect",
            SolidPlannerBot.class,
            SolidPlannerBot::new
        );

        return Map.copyOf(bots);
    }

    private static BotFactory reflectiveFactory(
        Class<? extends ChessBot> botClass
    ) {
        return () -> {
            try {
                return botClass
                    .getConstructor()
                    .newInstance();
            } catch (
                ReflectiveOperationException
                    | RuntimeException exception
            ) {
                throw new IllegalStateException(
                    "Impossible d'instancier le bot étudiant "
                        + botClass.getName(),
                    exception
                );
            }
        };
    }

    private static String studentKey(
        Class<? extends ChessBot> botClass
    ) {
        String simpleName =
            botClass.getSimpleName();

        String base =
            simpleName.endsWith("Bot")
                ? simpleName.substring(
                    0,
                    simpleName.length() - 3
                )
                : simpleName;

        String kebab =
            base.replaceAll(
                "([a-z0-9])([A-Z])",
                "$1-$2"
            ).replaceAll(
                "[^A-Za-z0-9]+",
                "-"
            ).replaceAll(
                "^-+|-+$",
                ""
            ).toLowerCase(Locale.ROOT);

        if (kebab.isBlank()) {
            throw new IllegalStateException(
                "Impossible de générer une clé pour "
                    + botClass.getName()
            );
        }

        return "student-" + kebab;
    }

    private static String normalizeKey(
        String name
    ) {
        return name.trim()
            .toLowerCase(Locale.ROOT);
    }

    private static void register(
        Map<String, Registration> registrations,
        String key,
        Class<? extends ChessBot> botClass,
        BotFactory factory
    ) {
        registrations.put(
            normalizeKey(key),
            new Registration(
                botClass,
                factory
            )
        );
    }

    private record Registration(
        Class<? extends ChessBot> botClass,
        BotFactory factory
    ) {
    }
}
