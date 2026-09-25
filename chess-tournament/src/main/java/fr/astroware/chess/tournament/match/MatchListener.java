package fr.astroware.chess.tournament.match;

import fr.astroware.chess.bot.api.BotMetadata;
import fr.astroware.chess.core.model.PositionView;

/**
 * Observer d'une partie.
 *
 * <p>La console, un futur écran live ou tout autre outil peuvent écouter la
 * partie sans modifier MatchRunner.</p>
 */
public interface MatchListener {

    default void onMatchStarted(
        BotMetadata white,
        BotMetadata black,
        PositionView initialPosition
    ) {
    }

    default void onMovePlayed(PlayedMove move) {
    }

    default void onMatchEnded(MatchResult result) {
    }
}
