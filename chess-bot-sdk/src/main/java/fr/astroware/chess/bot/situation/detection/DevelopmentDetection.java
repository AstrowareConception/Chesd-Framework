package fr.astroware.chess.bot.situation.detection;

import fr.astroware.chess.bot.rule.Detection;
import fr.astroware.chess.core.model.Move;
import fr.astroware.chess.core.model.PieceType;

import java.util.Objects;

/**
 * Coup légal développant une pièce mineure depuis sa case initiale.
 *
 * @param move coup de développement
 * @param pieceType cavalier ou fou développé
 * @param centerGain variation du contrôle du centre
 * @param mobilityGain variation de mobilité
 */
public record DevelopmentDetection(
    Move move,
    PieceType pieceType,
    double centerGain,
    double mobilityGain
) implements Detection {

    public DevelopmentDetection {
        Objects.requireNonNull(
            move,
            "move must not be null"
        );
        Objects.requireNonNull(
            pieceType,
            "pieceType must not be null"
        );

        if (pieceType != PieceType.KNIGHT
            && pieceType != PieceType.BISHOP) {
            throw new IllegalArgumentException(
                "DevelopmentDetection only supports minor pieces"
            );
        }

        if (!Double.isFinite(centerGain)
            || !Double.isFinite(mobilityGain)) {
            throw new IllegalArgumentException(
                "development gains must be finite"
            );
        }
    }
}
