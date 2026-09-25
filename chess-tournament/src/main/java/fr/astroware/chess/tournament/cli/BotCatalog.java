package fr.astroware.chess.tournament.cli;

import fr.astroware.chess.bots.baseline.BerserkerBot;
import fr.astroware.chess.bots.baseline.CautiousBot;
import fr.astroware.chess.bots.baseline.GreedyBot;
import fr.astroware.chess.bots.baseline.GuardianBot;
import fr.astroware.chess.bots.baseline.RandomBot;
import fr.astroware.chess.bots.baseline.TacticalBot;
import fr.astroware.chess.bots.examples.SolidPlannerBot;
import fr.astroware.chess.tournament.match.BotFactory;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Catalogue des bots livrés avec le framework.
 */
public final class BotCatalog {

    private static final Map<String, BotFactory> BOTS = createCatalog();

    private BotCatalog() {
    }

    public static Map<String, BotFactory> all() {
        return Map.copyOf(BOTS);
    }

    public static Optional<BotFactory> find(String name) {
        if (name == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(
            BOTS.get(name.trim().toLowerCase(Locale.ROOT))
        );
    }

    private static Map<String, BotFactory> createCatalog() {
        Map<String, BotFactory> bots = new LinkedHashMap<>();

        bots.put("random", RandomBot::new);
        bots.put("greedy", GreedyBot::new);
        bots.put("cautious", CautiousBot::new);
        bots.put("berserker", BerserkerBot::new);
        bots.put("guardian", GuardianBot::new);
        bots.put("tactical", TacticalBot::new);
        bots.put("architect", SolidPlannerBot::new);

        return bots;
    }
}
