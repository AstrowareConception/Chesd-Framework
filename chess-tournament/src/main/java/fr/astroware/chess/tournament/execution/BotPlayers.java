package fr.astroware.chess.tournament.execution;

import fr.astroware.chess.tournament.match.BotFactory;

import java.util.Objects;

/**
 * Fabriques d'exécution de bots.
 */
public final class BotPlayers {

    private BotPlayers() {
    }

    public static BotPlayerFactory<InProcessBotPlayer>
        inProcess(BotFactory factory) {

        Objects.requireNonNull(
            factory,
            "factory must not be null"
        );

        return ignoredSeed ->
            new InProcessBotPlayer(factory.create());
    }
}
