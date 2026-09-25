package fr.astroware.chess.tournament.execution;

/**
 * Fabrique un joueur pour une partie.
 *
 * @param <P> type concret de joueur
 */
@FunctionalInterface
public interface BotPlayerFactory<P extends BotPlayer> {

    /**
     * @param randomSeed graine dédiée à ce bot pour cette partie
     */
    P create(long randomSeed);
}
