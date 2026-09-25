package fr.astroware.chess.tournament.execution;

import fr.astroware.chess.bot.api.ChessBot;

import java.util.Objects;

/**
 * Fabrique une JVM isolée persistante pour une classe de bot.
 */
public final class IsolatedBotPlayerFactory
    implements BotPlayerFactory<IsolatedBotPlayer> {

    private final Class<? extends ChessBot> botClass;
    private final IsolatedBotSettings settings;

    public IsolatedBotPlayerFactory(
        Class<? extends ChessBot> botClass,
        IsolatedBotSettings settings
    ) {
        this.botClass = Objects.requireNonNull(
            botClass,
            "botClass must not be null"
        );
        this.settings = Objects.requireNonNull(
            settings,
            "settings must not be null"
        );
    }

    public static IsolatedBotPlayerFactory standard(
        Class<? extends ChessBot> botClass
    ) {
        return new IsolatedBotPlayerFactory(
            botClass,
            IsolatedBotSettings.standard()
        );
    }

    @Override
    public IsolatedBotPlayer create(
        long randomSeed
    ) {
        return new IsolatedBotPlayer(
            botClass,
            randomSeed,
            settings
        );
    }
}
