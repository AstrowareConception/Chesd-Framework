package fr.astroware.chess.bot.evaluation;

import fr.astroware.chess.core.model.Move;

import java.util.List;
import java.util.Objects;

/**
 * Un coup candidat accompagné de son évaluation.
 *
 * <p>En plus de sa qualité générale, un candidat peut décrire son caractère :
 * agressif, sûr ou risqué. Ces dimensions permettent à deux bots de choisir
 * différemment entre les mêmes coups selon leur profil stratégique.</p>
 *
 * @param move coup candidat
 * @param score qualité globale estimée du coup, de 0 à 10
 * @param aggression caractère offensif du coup, de 0 à 10
 * @param safety caractère protecteur/prudent du coup, de 0 à 10
 * @param risk niveau de risque ou d'incertitude du coup, de 0 à 10
 * @param criteria détail des critères ayant contribué à l'évaluation
 * @param explanation explication synthétique
 */
public record EvaluatedMove(
    Move move,
    EvaluationScore score,
    EvaluationScore aggression,
    EvaluationScore safety,
    EvaluationScore risk,
    List<EvaluationCriterion> criteria,
    String explanation
) {

    public EvaluatedMove {
        Objects.requireNonNull(move, "move must not be null");
        Objects.requireNonNull(score, "score must not be null");
        Objects.requireNonNull(aggression, "aggression must not be null");
        Objects.requireNonNull(safety, "safety must not be null");
        Objects.requireNonNull(risk, "risk must not be null");
        criteria = List.copyOf(
            Objects.requireNonNull(criteria, "criteria must not be null")
        );
        explanation = Objects.requireNonNullElse(explanation, "");
    }

    /**
     * Crée un candidat sans information stylistique particulière.
     *
     * <p>Les trois dimensions stratégiques reçoivent alors la valeur neutre
     * 5/10.</p>
     */
    public static EvaluatedMove of(
        Move move,
        double score,
        String explanation
    ) {
        EvaluationScore neutral = EvaluationScore.neutral();

        return new EvaluatedMove(
            move,
            EvaluationScore.of(score),
            neutral,
            neutral,
            neutral,
            List.of(),
            explanation
        );
    }

    /**
     * Crée un candidat en décrivant également sa personnalité stratégique.
     */
    public static EvaluatedMove strategic(
        Move move,
        double score,
        double aggression,
        double safety,
        double risk,
        String explanation
    ) {
        return new EvaluatedMove(
            move,
            EvaluationScore.of(score),
            EvaluationScore.of(aggression),
            EvaluationScore.of(safety),
            EvaluationScore.of(risk),
            List.of(),
            explanation
        );
    }
}
