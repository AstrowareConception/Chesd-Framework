package fr.astroware.chess.bot.api;

import fr.astroware.chess.bot.rule.RuleAttempt;
import fr.astroware.chess.core.model.Move;

import java.util.List;
import java.util.Objects;

/**
 * Décision finale produite par un bot.
 *
 * @param move coup choisi
 * @param trace règles évaluées jusqu'à la décision
 */
public record BotDecision(Move move, List<RuleAttempt> trace) {

    public BotDecision {
        Objects.requireNonNull(move, "move must not be null");
        trace = List.copyOf(Objects.requireNonNull(trace, "trace must not be null"));
    }
}
