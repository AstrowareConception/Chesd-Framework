package fr.astroware.chess.tournament.match;

import java.util.Optional;

/**
 * Configuration d'une partie entre deux bots.
 *
 * @param maxPlies nombre maximal de demi-coups
 * @param randomSeed graine de reproductibilité
 * @param initialFen position initiale personnalisée éventuelle
 */
public record MatchConfiguration(
    int maxPlies,
    long randomSeed,
    Optional<String> initialFen
) {

    public MatchConfiguration {
        if (maxPlies <= 0) {
            throw new IllegalArgumentException("maxPlies must be > 0");
        }

        initialFen = initialFen == null
            ? Optional.empty()
            : initialFen.map(String::trim).filter(value -> !value.isEmpty());
    }

    public static MatchConfiguration standard(long randomSeed) {
        return new MatchConfiguration(
            400,
            randomSeed,
            Optional.empty()
        );
    }

    public static MatchConfiguration fromFen(
        int maxPlies,
        long randomSeed,
        String fen
    ) {
        return new MatchConfiguration(
            maxPlies,
            randomSeed,
            Optional.of(fen)
        );
    }
}
