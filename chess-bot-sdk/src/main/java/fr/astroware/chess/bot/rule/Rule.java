package fr.astroware.chess.bot.rule;

import fr.astroware.chess.bot.api.BotContext;
import fr.astroware.chess.core.model.Move;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Association entre une situation et l'action à tenter lorsqu'elle est
 * reconnue.
 *
 * @param <D> type de détection partagé par la situation et l'action
 */
public final class Rule<D extends Detection> {

    private final String name;
    private final Situation<D> situation;
    private final Action<D> action;

    private Rule(String name, Situation<D> situation, Action<D> action) {
        this.name = requireName(name);
        this.situation = Objects.requireNonNull(
            situation,
            "situation must not be null"
        );
        this.action = Objects.requireNonNull(action, "action must not be null");
    }

    public static <D extends Detection> Rule<D> of(
        String name,
        Situation<D> situation,
        Action<D> action
    ) {
        return new Rule<>(name, situation, action);
    }

    /**
     * Évalue complètement la règle et produit une trace exploitable par
     * ChessBot.
     */
    public RuleAttempt evaluate(BotContext context) {
        Objects.requireNonNull(context, "context must not be null");

        try {
            List<D> detections = List.copyOf(situation.detect(context));

            if (detections.isEmpty()) {
                return new RuleAttempt(
                    name,
                    AttemptStatus.NOT_MATCHED,
                    0,
                    Optional.empty(),
                    "Situation non détectée"
                );
            }

            Optional<Move> proposedMove = action.choose(context, detections);

            if (proposedMove.isEmpty()) {
                return new RuleAttempt(
                    name,
                    AttemptStatus.MATCHED_NO_MOVE,
                    detections.size(),
                    Optional.empty(),
                    "Situation détectée mais aucune action applicable"
                );
            }

            Move move = proposedMove.orElseThrow();

            if (!context.legalMoves().contains(move)) {
                return new RuleAttempt(
                    name,
                    AttemptStatus.ILLEGAL_PROPOSAL,
                    detections.size(),
                    Optional.of(move),
                    "L'action a proposé un coup illégal"
                );
            }

            return new RuleAttempt(
                name,
                AttemptStatus.SELECTED,
                detections.size(),
                Optional.of(move),
                "Coup légal sélectionné"
            );
        } catch (RuntimeException exception) {
            return new RuleAttempt(
                name,
                AttemptStatus.ERROR,
                0,
                Optional.empty(),
                exception.getClass().getSimpleName()
                    + ": "
                    + Objects.toString(exception.getMessage(), "")
            );
        }
    }

    public String name() {
        return name;
    }

    private static String requireName(String name) {
        Objects.requireNonNull(name, "name must not be null");
        String normalized = name.trim();

        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("name must not be blank");
        }

        return normalized;
    }
}
