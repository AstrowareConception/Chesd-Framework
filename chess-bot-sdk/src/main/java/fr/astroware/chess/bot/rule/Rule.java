package fr.astroware.chess.bot.rule;

import fr.astroware.chess.bot.api.BotContext;
import fr.astroware.chess.bot.evaluation.EvaluatedMove;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Association entre une situation et l'action à tenter lorsqu'elle est
 * reconnue.
 *
 * <p>Une action peut produire plusieurs coups candidats évalués. La règle
 * conserve uniquement les coups légaux puis choisit celui dont la note est
 * la plus élevée. Ce comportement par défaut pourra ensuite être remplacé
 * par une politique de sélection plus spécialisée si nécessaire.</p>
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
                    List.of(),
                    Optional.empty(),
                    "Situation non détectée"
                );
            }

            List<EvaluatedMove> candidates = List.copyOf(
                action.evaluate(context, detections)
            );

            if (candidates.isEmpty()) {
                return new RuleAttempt(
                    name,
                    AttemptStatus.MATCHED_NO_MOVE,
                    detections.size(),
                    List.of(),
                    Optional.empty(),
                    "Situation détectée mais aucun coup candidat"
                );
            }

            List<EvaluatedMove> legalCandidates = new ArrayList<>();

            for (EvaluatedMove candidate : candidates) {
                if (context.legalMoves().contains(candidate.move())) {
                    legalCandidates.add(candidate);
                }
            }

            if (legalCandidates.isEmpty()) {
                return new RuleAttempt(
                    name,
                    AttemptStatus.ILLEGAL_PROPOSAL,
                    detections.size(),
                    candidates,
                    Optional.empty(),
                    "Aucun des coups candidats n'est légal"
                );
            }

            EvaluatedMove best = legalCandidates.getFirst();

            for (EvaluatedMove candidate : legalCandidates) {
                if (candidate.score().compareTo(best.score()) > 0) {
                    best = candidate;
                }
            }

            return new RuleAttempt(
                name,
                AttemptStatus.SELECTED,
                detections.size(),
                candidates,
                Optional.of(best),
                "Meilleur coup légal sélectionné avec une note de "
                    + best.score().value()
                    + "/10"
            );
        } catch (RuntimeException exception) {
            return new RuleAttempt(
                name,
                AttemptStatus.ERROR,
                0,
                List.of(),
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
