package fr.astroware.chess.bot.analysis;

import fr.astroware.chess.core.model.Move;

import java.util.Objects;
import java.util.Optional;

/**
 * Évaluation d'un coup après prise en compte de la meilleure réponse adverse.
 *
 * <p>Le score robuste est calculé du point de vue du bot qui envisage le
 * premier coup. L'adversaire est supposé choisir la réponse qui minimise ce
 * score.</p>
 *
 * @param move coup envisagé
 * @param immediateEvaluation évaluation juste après le coup
 * @param bestReply meilleure réponse adverse, absente si la partie est finie
 * @param afterReplyEvaluation évaluation après cette réponse, absente si
 *                             l'état est terminal
 * @param robustScore score pessimiste final, de 0 à 10
 * @param replyCount nombre de réponses adverses examinées
 * @param explanation explication pédagogique
 */
public record AdversarialEvaluation(
    Move move,
    PositionEvaluation immediateEvaluation,
    Optional<Move> bestReply,
    Optional<PositionEvaluation> afterReplyEvaluation,
    double robustScore,
    int replyCount,
    String explanation
) {

    public AdversarialEvaluation {
        Objects.requireNonNull(move, "move must not be null");
        Objects.requireNonNull(
            immediateEvaluation,
            "immediateEvaluation must not be null"
        );
        Objects.requireNonNull(bestReply, "bestReply must not be null");
        Objects.requireNonNull(
            afterReplyEvaluation,
            "afterReplyEvaluation must not be null"
        );
        Objects.requireNonNull(
            explanation,
            "explanation must not be null"
        );

        if (robustScore < 0.0 || robustScore > 10.0) {
            throw new IllegalArgumentException(
                "robustScore must be between 0 and 10"
            );
        }

        if (replyCount < 0) {
            throw new IllegalArgumentException(
                "replyCount must be non-negative"
            );
        }
    }
}
