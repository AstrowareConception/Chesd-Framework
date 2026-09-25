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
 * @param decisionNanos temps passé dans ChessBot.decide()
 * @param san notation algébrique courte du coup
 * @param beforeFen position avant le coup
 * @param afterFen position après le coup
 */
public record PlayedMove(
    int ply,
    Color color,
    BotMetadata bot,
    BotDecision decision,
    long decisionNanos,
    String san,
    String beforeFen,
    String afterFen
) {

    public PlayedMove {
        if (ply <= 0) {
            throw new IllegalArgumentException("ply must be > 0");
        }

        if (decisionNanos < 0L) {
            throw new IllegalArgumentException(
                "decisionNanos must be non-negative"
            );
        }

        Objects.requireNonNull(color, "color must not be null");
        Objects.requireNonNull(bot, "bot must not be null");
        Objects.requireNonNull(decision, "decision must not be null");
        Objects.requireNonNull(san, "san must not be null");
        Objects.requireNonNull(beforeFen, "beforeFen must not be null");
        Objects.requireNonNull(afterFen, "afterFen must not be null");
    }

    public int fullMoveNumber() {
        return (ply + 1) / 2;
    }

    public double decisionMillis() {
        return decisionNanos / 1_000_000.0;
    }
}
