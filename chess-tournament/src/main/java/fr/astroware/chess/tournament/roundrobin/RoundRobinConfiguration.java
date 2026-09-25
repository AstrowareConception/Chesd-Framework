package fr.astroware.chess.tournament.roundrobin;

/**
 * Configuration d'un tournoi toutes rondes.
 *
 * @param gamesPerPair nombre de parties pour chaque paire de bots
 * @param maxPlies limite technique de demi-coups par partie
 * @param baseSeed graine de base ; chaque partie dérive sa propre graine
 */
public record RoundRobinConfiguration(
    int gamesPerPair,
    int maxPlies,
    long baseSeed
) {

    public RoundRobinConfiguration {
        if (gamesPerPair <= 0) {
            throw new IllegalArgumentException(
                "gamesPerPair must be > 0"
            );
        }

        if (maxPlies <= 0) {
            throw new IllegalArgumentException(
                "maxPlies must be > 0"
            );
        }
    }

    /**
     * Deux parties par paire : chacun joue une fois avec les Blancs.
     */
    public static RoundRobinConfiguration standard(
        long baseSeed
    ) {
        return new RoundRobinConfiguration(
            2,
            400,
            baseSeed
        );
    }
}
