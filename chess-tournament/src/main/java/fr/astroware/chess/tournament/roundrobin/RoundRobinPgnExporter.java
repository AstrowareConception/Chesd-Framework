package fr.astroware.chess.tournament.roundrobin;

import fr.astroware.chess.tournament.pgn.PgnExporter;

import java.util.Objects;

/**
 * Exporte toutes les parties d'un tournoi dans un PGN multi-parties.
 */
public final class RoundRobinPgnExporter {

    private final PgnExporter exporter = new PgnExporter();

    public String export(RoundRobinResult result) {
        Objects.requireNonNull(
            result,
            "result must not be null"
        );

        String separator =
            System.lineSeparator()
                + System.lineSeparator();

        return result.matches().stream()
            .map(exporter::export)
            .collect(
                java.util.stream.Collectors.joining(separator)
            );
    }
}
