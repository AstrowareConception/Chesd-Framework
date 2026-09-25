package fr.astroware.chess.bot.situation.detection;

import fr.astroware.chess.bot.rule.Detection;
import fr.astroware.chess.core.model.Move;
import fr.astroware.chess.core.model.PlacedPiece;

import java.util.Objects;

/**
 * Sacrifice candidat à une attraction du roi.
 *
 * <p>Le coup donne échec et le roi adverse peut légalement capturer la pièce
 * offerte. Après cette capture, le camp attaquant dispose d'un gain tactique
 * concret : mat en un ou capture matérielle supérieure au sacrifice.</p>
 */
public record AttractionDetection(
    Move move,
    PlacedPiece sacrificedPiece,
    PlacedPiece king,
    Move kingCaptureReply,
    int sacrificedValue,
    boolean followUpMateInOne,
    int followUpCaptureValue
) implements Detection {

    public AttractionDetection {
        Objects.requireNonNull(move, "move must not be null");
        Objects.requireNonNull(
            sacrificedPiece,
            "sacrificedPiece must not be null"
        );
        Objects.requireNonNull(king, "king must not be null");
        Objects.requireNonNull(
            kingCaptureReply,
            "kingCaptureReply must not be null"
        );

        if (sacrificedValue < 0
            || followUpCaptureValue < 0) {
            throw new IllegalArgumentException(
                "attraction values must be non-negative"
            );
        }

        if (!followUpMateInOne
            && followUpCaptureValue
                <= sacrificedValue) {
            throw new IllegalArgumentException(
                "attraction must expose a concrete tactical follow-up"
            );
        }
    }
}
