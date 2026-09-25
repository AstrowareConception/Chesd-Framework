package fr.astroware.chess.tournament.match;

import fr.astroware.chess.bot.api.ChessBot;

/**
 * Fabrique une nouvelle instance de bot pour une partie.
 *
 * <p>Le tournoi ne doit pas réutiliser une instance entre plusieurs parties :
 * un bot pourra plus tard conserver un état interne lié à sa partie.</p>
 */
@FunctionalInterface
public interface BotFactory {

    ChessBot create();
}
