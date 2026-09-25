package fr.astroware.chess.bot.situation.detection;

import fr.astroware.chess.bot.rule.Detection;
import fr.astroware.chess.core.model.Move;
import fr.astroware.chess.core.model.PlacedPiece;

import java.util.Objects;

/**
 * Sacrifice d'attraction du roi.
 *
 * <p>Le coup donne échec, le roi adverse peut légalement capturer la pièce
 * offerte, et la case d'arrivée est contrôlée par le camp attaquant.</p>
 */
public record AttractionDetection(
    Move move,
    PlacedPiece sacrificedPiece,
    PlacedPiece king,
    int sacrificedValue,
    int defendersOnSacrificeSquare
) implements Detection {

    public AttractionDetection {
        Objects.requireNonNull(move, "move must not be null");
        Objects.requireNonNull(
            sacrificedPiece,
            "sacrificedPiece must not be null"
        );
        Objects.requireNonNull(king, "king must not be null");

        if (sacrificedValue < 0
            || defendersOnSacrificeSquare <= 0) {
            throw new IllegalArgumentException(
                "attraction values are invalid"
            );
        }
    }
}
