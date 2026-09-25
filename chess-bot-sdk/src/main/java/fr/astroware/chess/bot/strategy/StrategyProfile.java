package fr.astroware.chess.bot.strategy;

import fr.astroware.chess.bot.evaluation.EvaluatedMove;
import fr.astroware.chess.bot.evaluation.EvaluationScore;

import java.util.Objects;

/**
 * Préférences stratégiques générales d'un bot.
 *
 * <p>Le profil ne remplace pas les règles du bot. Il modifie la manière dont
 * plusieurs coups candidats sont comparés au sein d'une même règle.</p>
 *
 * <p>Trois axes sont volontairement proposés dans la première version :</p>
 * <ul>
 *     <li>agressivité : préférence pour l'initiative et les coups offensifs ;</li>
 *     <li>sécurité : préférence pour les coups prudents et protecteurs ;</li>
 *     <li>tolérance au risque : acceptation d'un coup incertain ou spéculatif.</li>
 * </ul>
 *
 * @param name nom lisible du profil
 * @param aggression préférence offensive, de 0 à 10
 * @param safety préférence défensive, de 0 à 10
 * @param riskTolerance tolérance au risque, de 0 à 10
 */
public record StrategyProfile(
    String name,
    EvaluationScore aggression,
    EvaluationScore safety,
    EvaluationScore riskTolerance
) {

    private static final double STYLE_FACTOR = 0.8;

    public StrategyProfile {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(aggression, "aggression must not be null");
        Objects.requireNonNull(safety, "safety must not be null");
        Objects.requireNonNull(riskTolerance, "riskTolerance must not be null");

        if (name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
    }

    /**
     * Calcule la préférence effective du profil pour un coup.
     *
     * <p>Le score de base reste prépondérant. Le style peut toutefois faire
     * préférer un coup légèrement moins bien noté s'il correspond davantage à
     * la personnalité du bot.</p>
     *
     * @param move coup évalué
     * @return score effectif normalisé entre 0 et 10
     */
    public EvaluationScore preferenceScore(EvaluatedMove move) {
        Objects.requireNonNull(move, "move must not be null");

        double adjustment = STYLE_FACTOR * (
            alignment(aggression, move.aggression())
                + alignment(safety, move.safety())
                + alignment(riskTolerance, move.risk())
        );

        double adjusted = Math.clamp(
            move.score().value() + adjustment,
            EvaluationScore.MIN,
            EvaluationScore.MAX
        );

        return EvaluationScore.of(adjusted);
    }

    private static double alignment(
        EvaluationScore preference,
        EvaluationScore characteristic
    ) {
        double centeredPreference =
            (preference.value() - EvaluationScore.NEUTRAL) / 5.0;
        double centeredCharacteristic =
            (characteristic.value() - EvaluationScore.NEUTRAL) / 5.0;

        return centeredPreference * centeredCharacteristic;
    }
}
