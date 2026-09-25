package fr.astroware.chess.tournament.roundrobin;

import fr.astroware.chess.tournament.match.BotFactory;

import java.util.Objects;

/**
 * Participant déclaré dans un tournoi.
 *
 * @param key identifiant court utilisé par le CLI
 * @param factory fabrique une nouvelle instance du bot pour chaque partie
 */
public record TournamentParticipant(
    String key,
    BotFactory factory
) {

    public TournamentParticipant {
        Objects.requireNonNull(key, "key must not be null");
        Objects.requireNonNull(factory, "factory must not be null");

        key = key.trim();

        if (key.isEmpty()) {
            throw new IllegalArgumentException(
                "key must not be blank"
            );
        }
    }
}
