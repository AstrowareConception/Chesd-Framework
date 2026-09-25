package fr.astroware.chess.tournament.match;

import fr.astroware.chess.bot.api.BotMetadata;
import fr.astroware.chess.core.model.Color;

import java.util.Objects;

/**
 * Incident attribué à un bot pendant une partie.
 *
 * @param type catégorie d'incident
 * @param offenderColor couleur du bot fautif
 * @param offender identité du bot fautif
 * @param exceptionClass classe de l'exception
 * @param message message nettoyé de l'exception
 * @param ply demi-coup pendant lequel l'incident est survenu
 */
public record MatchIncident(
    MatchIncidentType type,
    Color offenderColor,
    BotMetadata offender,
    String exceptionClass,
    String message,
    int ply
) {

    public MatchIncident {
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(
            offenderColor,
            "offenderColor must not be null"
        );
        Objects.requireNonNull(
            offender,
            "offender must not be null"
        );
        exceptionClass = Objects.requireNonNullElse(
            exceptionClass,
            ""
        );
        message = Objects.requireNonNullElse(
            message,
            ""
        );

        if (ply <= 0) {
            throw new IllegalArgumentException(
                "ply must be > 0"
            );
        }
    }

    public String summary() {
        String detail = message.isBlank()
            ? exceptionClass
            : exceptionClass + ": " + message;

        return offender.botName()
            + " ("
            + offenderColor
            + ") — "
            + type
            + (detail.isBlank() ? "" : " — " + detail);
    }
}
