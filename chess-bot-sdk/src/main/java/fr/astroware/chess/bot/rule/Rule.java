package fr.astroware.chess.bot.rule;

import fr.astroware.chess.bot.api.BotContext;
import fr.astroware.chess.bot.evaluation.EvaluatedMove;
import fr.astroware.chess.bot.evaluation.EvaluationScore;
import fr.astroware.chess.bot.strategy.StrategyProfile;
import fr.astroware.chess.bot.strategy.StrategyProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Association entre une situation et l'action à tenter lorsqu'elle est
 * reconnue.
 *
 * <p>Une action peut produire plusieurs coups candidats évalués. La règle
 * conserve uniquement les coups légaux puis demande au profil stratégique du
 * bot de départager les candidats.</p>
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
     * Évalue une règle avec un profil équilibré.
     *
     * <p>Cette surcharge reste pratique pour les tests unitaires et les usages
     * simples hors d'un ChessBot.</p>
     */
    public RuleAttempt evaluate(BotContext context) {
        return evaluate(context, StrategyProfiles.balanced());
    }

    /**
     * Évalue complètement la règle avec la personnalité stratégique du bot.
     */
    public RuleAttempt evaluate(
        BotContext context,
        StrategyProfile strategyProfile
    ) {
        Objects.requireNonNull(context, "context must not be null");
        Objects.requireNonNull(
            strategyProfile,
            "strategyProfile must not be null"
        );

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
            EvaluationScore bestPreference =
                strategyProfile.preferenceScore(best);

            for (EvaluatedMove candidate : legalCandidates) {
                EvaluationScore preference =
                    strategyProfile.preferenceScore(candidate);

                if (preference.compareTo(bestPreference) > 0) {
                    best = candidate;
                    bestPreference = preference;
                }
            }

            return new RuleAttempt(
                name,
                AttemptStatus.SELECTED,
                detections.size(),
                candidates,
                Optional.of(best),
                "Coup sélectionné par le profil « "
                    + strategyProfile.name()
                    + " » : score de base "
                    + best.score().value()
                    + "/10, préférence effective "
                    + bestPreference.value()
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
