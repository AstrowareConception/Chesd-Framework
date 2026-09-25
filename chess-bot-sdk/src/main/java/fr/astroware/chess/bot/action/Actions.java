package fr.astroware.chess.bot.action;

import fr.astroware.chess.bot.analysis.PositionProjection;
import fr.astroware.chess.bot.evaluation.EvaluatedMove;
import fr.astroware.chess.bot.rule.Action;
import fr.astroware.chess.bot.rule.Detection;
import fr.astroware.chess.bot.situation.detection.CaptureDetection;
import fr.astroware.chess.bot.situation.detection.ThreatenedPieceDetection;
import fr.astroware.chess.core.model.Color;
import fr.astroware.chess.core.model.Move;
import fr.astroware.chess.core.model.Piece;
import fr.astroware.chess.core.model.PlacedPiece;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Point d'entrée vers les actions réutilisables fournies par le framework.
 */
public final class Actions {

    private Actions() {
    }

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
     * Politique matérialiste volontairement simple.
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

                return EvaluatedMove.strategic(
                    detection.move(),
                    score,
                    8.0,
                    10.0 - risk,
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
     * Évalue une capture en tenant compte de la position réellement obtenue.
     *
     * <p>Lorsque le contexte possède un FEN complet, le coup est joué sur une
     * projection du vrai moteur : les lignes ouvertes par la capture, les
     * nouvelles attaques et les défenseurs sont donc pris en compte. Les
     * contextes pédagogiques minimalistes sans FEN conservent l'heuristique
     * locale afin de rester faciles à tester.</p>
     */
    public static Action<CaptureDetection> captureWithRiskAwareness() {
        return (context, detections) -> detections.stream()
            .map(detection -> {
                RiskAssessment riskAssessment =
                    assessCaptureRisk(context, detection);

                double materialInterest =
                    4.0 + detection.targetValue() * 0.55;

                double score = Math.clamp(
                    materialInterest - riskAssessment.penalty(),
                    0.0,
                    10.0
                );

                return EvaluatedMove.strategic(
                    detection.move(),
                    score,
                    6.0,
                    10.0 - riskAssessment.risk(),
                    riskAssessment.risk(),
                    riskAssessment.explanation()
                );
            })
            .toList();
    }

    /**
     * Cherche les cases permettant de sauver une pièce pendue.
     *
     * <p>Chaque déplacement est réellement simulé. Une case où la pièce reste
     * attaquée et non défendue reçoit donc une mauvaise note, même si elle
     * semblait géométriquement éloignée du danger avant le déplacement.</p>
     */
    public static Action<ThreatenedPieceDetection> moveThreatenedPieceToSafety() {
        return (context, detections) -> {
            List<EvaluatedMove> candidates = new ArrayList<>();

            for (ThreatenedPieceDetection detection : detections) {
                for (Move move : context.legalMoves()) {
                    if (!move.from().equals(detection.piece().square())) {
                        continue;
                    }

                    PositionProjection projection =
                        context.analysis().after(move);

                    Optional<Piece> movedPiece =
                        projection.position().pieceAt(move.to());

                    if (movedPiece.isEmpty()) {
                        continue;
                    }

                    PlacedPiece projectedPiece =
                        new PlacedPiece(movedPiece.orElseThrow(), move.to());

                    boolean attacked =
                        projection.analysis().isAttacked(projectedPiece);
                    boolean defended =
                        projection.analysis().isDefended(projectedPiece);

                    double safety;
                    double risk;
                    double score;

                    if (!attacked) {
                        safety = 9.5;
                        risk = 1.0;
                        score = 8.0 + Math.min(1.5, detection.value() * 0.12);
                    } else if (defended) {
                        safety = 6.0;
                        risk = 5.0;
                        score = 5.5 + Math.min(1.0, detection.value() * 0.08);
                    } else {
                        safety = 2.0;
                        risk = 9.0;
                        score = 2.5;
                    }

                    boolean captures = context.position()
                        .pieceAt(move.to())
                        .filter(piece ->
                            piece.color() == context.myColor().opposite()
                        )
                        .isPresent();

                    candidates.add(
                        EvaluatedMove.strategic(
                            move,
                            Math.clamp(score, 0.0, 10.0),
                            captures ? 7.0 : 3.0,
                            safety,
                            risk,
                            "Sauver "
                                + detection.piece().piece().type()
                                + " : après "
                                + move.toUci()
                                + ", pièce "
                                + (attacked ? "attaquée" : "non attaquée")
                                + (defended ? " et défendue" : "")
                        )
                    );
                }
            }

            return List.copyOf(candidates);
        };
    }

    private static RiskAssessment assessCaptureRisk(
        fr.astroware.chess.bot.api.BotContext context,
        CaptureDetection detection
    ) {
        String fen = context.position().fen();

        if (fen != null && !fen.isBlank()) {
            PositionProjection projection =
                context.analysis().after(detection.move());

            Optional<Piece> movedPiece =
                projection.position().pieceAt(detection.move().to());

            if (movedPiece.isPresent()) {
                PlacedPiece projectedPiece = new PlacedPiece(
                    movedPiece.orElseThrow(),
                    detection.move().to()
                );

                boolean attacked =
                    projection.analysis().isAttacked(projectedPiece);
                boolean defended =
                    projection.analysis().isDefended(projectedPiece);

                if (!attacked) {
                    return new RiskAssessment(
                        1.0,
                        0.0,
                        "Capture sûre après projection : la pièce n'est plus attaquée"
                    );
                }

                if (defended) {
                    double penalty = Math.max(
                        0.8,
                        (detection.attackerValue() - detection.targetValue())
                            * 0.55
                    );

                    return new RiskAssessment(
                        5.5,
                        penalty,
                        "Capture projetée : la pièce reste attaquée mais est défendue"
                    );
                }

                double penalty = 1.4
                    + Math.max(
                        0,
                        detection.attackerValue() - detection.targetValue()
                    ) * 0.7;

                return new RiskAssessment(
                    9.0,
                    penalty,
                    "Capture projetée dangereuse : la pièce devient pendue"
                );
            }
        }

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

        double penalty = detection.targetDefenders() == 0
            ? 0.0
            : 1.2
                + Math.max(
                    0,
                    detection.attackerValue()
                        - detection.targetValue()
                ) * 0.7
                + Math.min(2.0, detection.targetDefenders() * 0.5);

        return new RiskAssessment(
            risk,
            penalty,
            "Capture évaluée par l'heuristique locale de défense"
        );
    }

    private record RiskAssessment(
        double risk,
        double penalty,
        String explanation
    ) {
    }
}
