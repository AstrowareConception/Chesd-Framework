package fr.astroware.chess.bot.strategy;

import fr.astroware.chess.bot.evaluation.EvaluationScore;

/**
 * Profils stratégiques prêts à l'emploi.
 *
 * <p>Ils constituent des points de départ. Un étudiant peut ensuite créer son
 * propre {@link StrategyProfile} pour donner une personnalité plus précise à
 * son bot.</p>
 */
public final class StrategyProfiles {

    private StrategyProfiles() {
    }

    public static StrategyProfile balanced() {
        return profile("Équilibré", 5.0, 5.0, 5.0);
    }

    public static StrategyProfile defensive() {
        return profile("Défensif", 3.0, 9.0, 2.0);
    }

    public static StrategyProfile aggressive() {
        return profile("Offensif", 9.0, 4.0, 7.0);
    }

    public static StrategyProfile adventurous() {
        return profile("Aventurier", 9.0, 3.0, 10.0);
    }

    public static StrategyProfile solid() {
        return profile("Solide", 4.0, 8.0, 3.0);
    }

    public static StrategyProfile profile(
        String name,
        double aggression,
        double safety,
        double riskTolerance
    ) {
        return new StrategyProfile(
            name,
            EvaluationScore.of(aggression),
            EvaluationScore.of(safety),
            EvaluationScore.of(riskTolerance)
        );
    }
}
