package fr.astroware.chess.bot.evaluation;

/**
 * Note normalisée utilisée par le framework pour comparer une position ou un
 * coup candidat.
 *
 * <p>L'échelle va de 0 à 10 :</p>
 * <ul>
 *     <li>0 : très mauvais / catastrophique ;</li>
 *     <li>5 : neutre ou moyen ;</li>
 *     <li>10 : excellent.</li>
 * </ul>
 *
 * <p>Cette note reste une heuristique. Elle ne prétend pas représenter une
 * vérité absolue sur une position d'échecs.</p>
 *
 * @param value valeur comprise entre 0 et 10
 */
public record EvaluationScore(double value)
    implements Comparable<EvaluationScore> {

    public static final double MIN = 0.0;
    public static final double MAX = 10.0;
    public static final double NEUTRAL = 5.0;

    public EvaluationScore {
        if (!Double.isFinite(value) || value < MIN || value > MAX) {
            throw new IllegalArgumentException(
                "Evaluation score must be between 0 and 10: " + value
            );
        }
    }

    public static EvaluationScore of(double value) {
        return new EvaluationScore(value);
    }

    public static EvaluationScore neutral() {
        return new EvaluationScore(NEUTRAL);
    }

    @Override
    public int compareTo(EvaluationScore other) {
        return Double.compare(value, other.value);
    }
}
