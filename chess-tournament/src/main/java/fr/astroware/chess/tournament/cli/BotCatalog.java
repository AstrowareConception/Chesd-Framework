package fr.astroware.chess.tournament.cli;

import fr.astroware.chess.bot.api.ChessBot;
import fr.astroware.chess.bots.baseline.BerserkerBot;
import fr.astroware.chess.bots.baseline.CautiousBot;
import fr.astroware.chess.bots.baseline.ChameleonBot;
import fr.astroware.chess.bots.baseline.GreedyBot;
import fr.astroware.chess.bots.baseline.GuardianBot;
import fr.astroware.chess.bots.baseline.LookaheadBot;
import fr.astroware.chess.bots.baseline.MinimaxBot;
import fr.astroware.chess.bots.baseline.RandomBot;
import fr.astroware.chess.bots.baseline.PressureBot;
import fr.astroware.chess.bots.baseline.PositionalBot;
import fr.astroware.chess.bots.baseline.TacticalBot;
import fr.astroware.chess.bots.examples.SolidPlannerBot;
import fr.astroware.chess.tournament.match.BotFactory;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Catalogue des bots livrés avec le framework.
 *
 * <p>Le catalogue conserve à la fois la fabrique historique et la classe du
 * bot. La classe est nécessaire pour démarrer une JVM isolée.</p>
 */
public final class BotCatalog {

    private static final Map<String, Registration>
        REGISTRATIONS = createCatalog();

    private BotCatalog() {
    }

    public static Map<String, BotFactory> all() {
        Map<String, BotFactory> factories =
            new LinkedHashMap<>();

        REGISTRATIONS.forEach(
            (key, registration) ->
                factories.put(
                    key,
                    registration.factory()
                )
        );

        return Map.copyOf(factories);
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
                name.trim()
                    .toLowerCase(Locale.ROOT)
            )
        );
    }

    private static Map<String, Registration>
        createCatalog() {

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

    private static void register(
        Map<String, Registration> registrations,
        String key,
        Class<? extends ChessBot> botClass,
        BotFactory factory
    ) {
        registrations.put(
            key,
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
