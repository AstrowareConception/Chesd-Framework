package fr.astroware.chess.tournament.execution;

import fr.astroware.chess.bot.api.BotContext;
import fr.astroware.chess.bot.api.BotDecision;
import fr.astroware.chess.bot.api.BotMetadata;
import fr.astroware.chess.bot.api.ChessBot;

import java.util.Objects;

/**
 * Adaptateur d'un ChessBot exécuté directement dans la JVM du tournoi.
 */
public final class InProcessBotPlayer implements BotPlayer {

    private final ChessBot bot;
    private final BotMetadata metadata;

    public InProcessBotPlayer(ChessBot bot) {
        this.bot = Objects.requireNonNull(
            bot,
            "bot must not be null"
        );
        this.metadata = Objects.requireNonNull(
            bot.metadata(),
            "bot metadata must not be null"
        );
    }

    @Override
    public BotMetadata metadata() {
        return metadata;
    }

    @Override
    public BotDecision decide(BotContext context) {
        return bot.decide(context);
    }
}
