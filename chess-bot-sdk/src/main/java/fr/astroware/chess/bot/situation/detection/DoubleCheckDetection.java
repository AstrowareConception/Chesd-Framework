package fr.astroware.chess.bot.situation.detection;

import fr.astroware.chess.bot.rule.Detection;
import fr.astroware.chess.core.model.Move;
import fr.astroware.chess.core.model.PlacedPiece;

import java.util.List;
import java.util.Objects;

/**
 * Coup créant un double échec.
 *
 * @param move coup joué
 * @param attackers pièces attaquant simultanément le roi adverse
 */
public record DoubleCheckDetection(
    Move move,
    List<PlacedPiece> attackers
) implements Detection {

    public DoubleCheckDetection {
        Objects.requireNonNull(move, "move must not be null");
        attackers = List.copyOf(
            Objects.requireNonNull(attackers, "attackers must not be null")
        );

        if (attackers.size() < 2) {
            throw new IllegalArgumentException(
                "A double check requires at least two attackers"
            );
        }
    }
}
