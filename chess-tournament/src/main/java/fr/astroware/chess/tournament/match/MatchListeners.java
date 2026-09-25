package fr.astroware.chess.tournament.match;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Outils de composition des listeners de match.
 */
public final class MatchListeners {

    private MatchListeners() {
    }

    public static MatchListener none() {
        return new MatchListener() {
        };
    }

    public static MatchListener combine(MatchListener... listeners) {
        List<MatchListener> delegates = Arrays.stream(listeners)
            .filter(Objects::nonNull)
            .toList();

        return new MatchListener() {
            @Override
            public void onMatchStarted(
                fr.astroware.chess.bot.api.BotMetadata white,
                fr.astroware.chess.bot.api.BotMetadata black,
                fr.astroware.chess.core.model.PositionView initialPosition
            ) {
                delegates.forEach(listener ->
                    listener.onMatchStarted(white, black, initialPosition)
                );
            }

            @Override
            public void onMovePlayed(PlayedMove move) {
                delegates.forEach(listener -> listener.onMovePlayed(move));
            }

            @Override
            public void onMatchEnded(MatchResult result) {
                delegates.forEach(listener -> listener.onMatchEnded(result));
            }
        };
    }
}
