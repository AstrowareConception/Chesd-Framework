package fr.astroware.chess.bot.api;

import fr.astroware.chess.bot.analysis.Analysis;
import fr.astroware.chess.bot.evaluation.EvaluatedMove;
import fr.astroware.chess.bot.rule.AttemptStatus;
import fr.astroware.chess.bot.rule.Rule;
import fr.astroware.chess.bot.rule.RuleAttempt;
import fr.astroware.chess.bot.strategy.StrategyProfile;
import fr.astroware.chess.bot.strategy.StrategyProfiles;
import fr.astroware.chess.core.model.Color;
import fr.astroware.chess.core.model.Move;
import fr.astroware.chess.core.model.PositionView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.random.RandomGenerator;

/**
 * Classe de base de tous les bots du framework.
 */
public abstract class ChessBot {

    public abstract BotMetadata metadata();

    protected abstract List<Rule<?>> rules();

    protected StrategyProfile strategyProfile() {
        return StrategyProfiles.balanced();
    }

    /**
     * Exécute le cycle standard de décision.
     *
     * <p>Le contexte est enveloppé pour partager une même instance
     * d'{@link Analysis} entre toutes les règles de ce tour. Les projections
     * calculées par une tactique peuvent ainsi être réutilisées par les
     * suivantes.</p>
     */
    public final BotDecision decide(BotContext context) {
        Objects.requireNonNull(context, "context must not be null");

        Analysis analysis = context.analysis();
        BotContext decisionContext =
            new DecisionContext(context, analysis);

        List<Move> legalMoves =
            List.copyOf(decisionContext.legalMoves());

        if (legalMoves.isEmpty()) {
            throw new IllegalStateException(
                "ChessBot cannot decide when there is no legal move"
            );
        }

        StrategyProfile profile = Objects.requireNonNull(
            strategyProfile(),
            "strategyProfile must not return null"
        );

        List<RuleAttempt> trace = new ArrayList<>();

        for (Rule<?> rule : List.copyOf(rules())) {
            RuleAttempt attempt =
                rule.evaluate(decisionContext, profile);
            trace.add(attempt);

            if (attempt.status() == AttemptStatus.SELECTED) {
                return new BotDecision(
                    attempt.selectedMove().orElseThrow().move(),
                    trace
                );
            }
        }

        Move fallback = legalMoves.get(
            decisionContext.random().nextInt(legalMoves.size())
        );

        EvaluatedMove evaluatedFallback = EvaluatedMove.of(
            fallback,
            5.0,
            "Fallback aléatoire du framework"
        );

        trace.add(new RuleAttempt(
            "Framework fallback",
            AttemptStatus.SELECTED,
            0,
            List.of(evaluatedFallback),
            Optional.of(evaluatedFallback),
            "Aucune règle du bot n'a produit de coup légal"
        ));

        return new BotDecision(fallback, trace);
    }

    protected final <D extends fr.astroware.chess.bot.rule.Detection> Rule<D> rule(
        String name,
        fr.astroware.chess.bot.rule.Situation<D> situation,
        fr.astroware.chess.bot.rule.Action<D> action
    ) {
        return Rule.of(name, situation, action);
    }

    private record DecisionContext(
        BotContext delegate,
        Analysis analysis
    ) implements BotContext {

        private DecisionContext {
            Objects.requireNonNull(delegate, "delegate must not be null");
            Objects.requireNonNull(analysis, "analysis must not be null");
        }

        @Override
        public Color myColor() {
            return delegate.myColor();
        }

        @Override
        public PositionView position() {
            return delegate.position();
        }

        @Override
        public List<Move> legalMoves() {
            return delegate.legalMoves();
        }

        @Override
        public List<Move> moveHistory() {
            return delegate.moveHistory();
        }

        @Override
        public RandomGenerator random() {
            return delegate.random();
        }
    }
}
