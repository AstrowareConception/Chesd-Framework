package fr.astroware.chess.tournament.match;

import fr.astroware.chess.bot.api.BotDecision;
import fr.astroware.chess.bot.api.BotMetadata;
import fr.astroware.chess.core.model.Color;

import java.util.Objects;

/**
 * Un demi-coup réellement joué pendant un match.
 *
 * @param ply numéro du demi-coup, à partir de 1
 * @param color camp ayant joué
 * @param bot identité du bot
 * @param decision décision complète, trace incluse
 */
public record PlayedMove(
    int ply,
    Color color,
    BotMetadata bot,
    BotDecision decision
) {

    public PlayedMove {
        if (ply <= 0) {
            throw new IllegalArgumentException("ply must be > 0");
        }

        Objects.requireNonNull(color, "color must not be null");
        Objects.requireNonNull(bot, "bot must not be null");
        Objects.requireNonNull(decision, "decision must not be null");
    }
}
