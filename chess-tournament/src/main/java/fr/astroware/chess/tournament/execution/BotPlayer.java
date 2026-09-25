package fr.astroware.chess.tournament.execution;

import fr.astroware.chess.bot.api.BotContext;
import fr.astroware.chess.bot.api.BotDecision;
import fr.astroware.chess.bot.api.BotMetadata;

/**
 * Bot exécutable par le moteur de tournoi.
 *
 * <p>L'implémentation peut vivre dans la JVM du tournoi ou dans un processus
 * isolé. MatchRunner ne dépend que de ce contrat.</p>
 */
public interface BotPlayer extends AutoCloseable {

    BotMetadata metadata();

    BotDecision decide(BotContext context);

    @Override
    default void close() {
        // Rien à libérer pour un bot en mémoire.
    }
}
