package fr.astroware.chess.bot.action;

import fr.astroware.chess.bot.evaluation.EvaluatedMove;
import fr.astroware.chess.bot.rule.Action;
import fr.astroware.chess.bot.rule.Detection;
import fr.astroware.chess.bot.situation.detection.CaptureDetection;
import fr.astroware.chess.core.model.Move;

import java.util.List;

/**
 * Point d'entrée vers les actions réutilisables fournies par le framework.
 */
public final class Actions {

    private Actions() {
    }

    /**
     * Choisit aléatoirement un coup parmi les coups légaux.
     */
    public static <D extends Detection> Action<D> randomLegalMove() {
        return (context, detections) -> {
            List<Move> legalMoves = context.legalMoves();

            if (legalMoves.isEmpty()) {
                return List.of();
            }

            Move move = legalMoves.get(
                context.random().nextInt(legalMoves.size())
            );

            return List.of(
                EvaluatedMove.of(
                    move,
                    5.0,
                    "Coup légal choisi aléatoirement"
                )
            );
        };
    }

    /**
     * Évalue une capture principalement selon la valeur de la cible.
     *
     * <p>Cette action est volontairement naïve : elle illustre une politique
     * "matérialiste" facile à comprendre. Elle constitue la base de GreedyBot.</p>
     */
    public static Action<CaptureDetection> captureHighestValue() {
        return (context, detections) -> detections.stream()
            .map(detection -> {
                double score = Math.clamp(
                    4.5 + detection.targetValue() * 0.6,
                    0.0,
                    10.0
                );

                double risk = detection.targetDefenders() == 0
                    ? 2.0
                    : Math.clamp(
                        5.0
                            + detection.targetDefenders()
                            + Math.max(
                                0,
                                detection.attackerValue()
                                    - detection.targetValue()
                            ) * 0.5,
                        0.0,
                        10.0
                    );

                double safety = 10.0 - risk;

                return EvaluatedMove.strategic(
                    detection.move(),
                    score,
                    8.0,
                    safety,
                    risk,
                    "Capture de "
                        + detection.target().piece().type()
                        + " (valeur "
                        + detection.targetValue()
                        + "), défendue par "
                        + detection.targetDefenders()
                        + " pièce(s)"
                );
            })
            .toList();
    }

    /**
     * Évalue une capture en pénalisant fortement les recaptures probables.
     *
     * <p>Cette heuristique ne remplace pas encore une projection complète de la
     * position. Elle utilise toutefois le nombre de défenseurs de la cible et
     * le rapport de valeur entre attaquant et cible pour estimer le risque.</p>
     */
    public static Action<CaptureDetection> captureWithRiskAwareness() {
        return (context, detections) -> detections.stream()
            .map(detection -> {
                double materialInterest =
                    4.0 + detection.targetValue() * 0.55;

                double exchangePenalty = detection.targetDefenders() == 0
                    ? 0.0
                    : 1.2
                        + Math.max(
                            0,
                            detection.attackerValue()
                                - detection.targetValue()
                        ) * 0.7
                        + Math.min(2.0, detection.targetDefenders() * 0.5);

                double score = Math.clamp(
                    materialInterest - exchangePenalty,
                    0.0,
                    10.0
                );

                double risk = detection.targetDefenders() == 0
                    ? 1.5
                    : Math.clamp(
                        4.0
                            + detection.targetDefenders()
                            + Math.max(
                                0,
                                detection.attackerValue()
                                    - detection.targetValue()
                            ),
                        0.0,
                        10.0
                    );

                return EvaluatedMove.strategic(
                    detection.move(),
                    score,
                    6.0,
                    10.0 - risk,
                    risk,
                    "Capture évaluée avec prudence : cible "
                        + detection.target().piece().type()
                        + ", attaquant "
                        + detection.attacker().piece().type()
                        + ", défenseurs "
                        + detection.targetDefenders()
                );
            })
            .toList();
    }
}
