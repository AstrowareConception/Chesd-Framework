package fr.astroware.chess.bot.action;

import fr.astroware.chess.bot.analysis.PositionProjection;
import fr.astroware.chess.bot.evaluation.EvaluatedMove;
import fr.astroware.chess.bot.rule.Action;
import fr.astroware.chess.bot.rule.Detection;
import fr.astroware.chess.bot.rule.PresenceDetection;
import fr.astroware.chess.bot.situation.detection.CaptureDetection;
import fr.astroware.chess.bot.situation.detection.DiscoveredAttackDetection;
import fr.astroware.chess.bot.situation.detection.DoubleCheckDetection;
import fr.astroware.chess.bot.situation.detection.CheckingMoveDetection;
import fr.astroware.chess.bot.situation.detection.ForkDetection;
import fr.astroware.chess.bot.situation.detection.MateInOneDetection;
import fr.astroware.chess.bot.situation.detection.PinDetection;
import fr.astroware.chess.bot.situation.detection.RemoveDefenderDetection;
import fr.astroware.chess.bot.situation.detection.SkewerDetection;
import fr.astroware.chess.bot.situation.detection.ThreatenedPieceDetection;
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
     * Évalue toutes les sorties d'échec légales.
     *
     * <p>Le moteur ne fournit déjà que des coups légaux : chaque candidat
     * résout donc l'échec. L'action privilégie ensuite le matériel conservé,
     * les captures utiles et la sécurité de la pièce déplacée.</p>
     */
    public static Action<PresenceDetection> bestCheckEscape() {
        return (context, detections) -> context.legalMoves().stream()
            .map(move -> {
                PositionProjection projection =
                    context.analysis().after(move);

                int materialAdvantage = projection.analysis()
                    .materialBalance()
                    .advantageFor(context.myColor());

                boolean captures = context.position()
                    .pieceAt(move.to())
                    .filter(piece ->
                        piece.color() == context.myColor().opposite()
                    )
                    .isPresent();

                Optional<Piece> moved =
                    projection.position().pieceAt(move.to());

                boolean attacked = false;
                boolean defended = false;

                if (moved.isPresent()
                    && moved.orElseThrow().color() == context.myColor()) {
                    PlacedPiece placed = new PlacedPiece(
                        moved.orElseThrow(),
                        move.to()
                    );
                    attacked = projection.analysis().isAttacked(placed);
                    defended = projection.analysis().isDefended(placed);
                }

                double safety = !attacked
                    ? 9.0
                    : defended ? 6.0 : 2.5;

                double risk = 10.0 - safety;

                double score = Math.clamp(
                    7.5
                        + materialAdvantage * 0.12
                        + (captures ? 0.6 : 0.0)
                        - (attacked && !defended ? 1.1 : 0.0),
                    0.0,
                    10.0
                );

                return EvaluatedMove.strategic(
                    move,
                    score,
                    captures ? 6.5 : 3.0,
                    safety,
                    risk,
                    "Sortie d'échec légale"
                );
            })
            .toList();
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

    public static Action<MateInOneDetection> playMateInOne() {
        return (context, detections) -> detections.stream()
            .map(detection -> EvaluatedMove.strategic(
                detection.move(),
                10.0,
                10.0,
                10.0,
                0.0,
                "Échec et mat immédiat"
            ))
            .toList();
    }

    /**
     * Évalue les coups d'échec : moins l'adversaire possède de réponses,
     * plus le coup est considéré comme forçant.
     */
    public static Action<CheckingMoveDetection> playBestCheck() {
        return (context, detections) -> detections.stream()
            .map(detection -> {
                double safety = !detection.movedPieceAttacked()
                    ? 8.5
                    : detection.movedPieceDefended() ? 6.0 : 2.5;

                double risk = 10.0 - safety;
                double forcingBonus = Math.max(
                    0.0,
                    2.2 - detection.opponentReplies() * 0.15
                );

                double score = Math.clamp(
                    7.1
                        + forcingBonus
                        - (detection.movedPieceAttacked()
                            && !detection.movedPieceDefended()
                            ? 1.0
                            : 0.0),
                    0.0,
                    9.8
                );

                return EvaluatedMove.strategic(
                    detection.move(),
                    score,
                    9.0,
                    safety,
                    risk,
                    "Échec laissant "
                        + detection.opponentReplies()
                        + " réponse(s) légale(s)"
                );
            })
            .toList();
    }

    public static Action<ForkDetection> playBestFork() {
        return (context, detections) -> detections.stream()
            .map(detection -> {
                double risk;
                double safety;
                double penalty;

                if (!detection.attackerAttacked()) {
                    risk = 2.0;
                    safety = 9.0;
                    penalty = 0.0;
                } else if (detection.attackerDefended()) {
                    risk = 5.0;
                    safety = 6.0;
                    penalty = 0.7;
                } else {
                    risk = 9.0;
                    safety = 2.0;
                    penalty = 2.0;
                }

                double score = Math.clamp(
                    5.5 + detection.targetValueSum() * 0.45 - penalty,
                    0.0,
                    9.8
                );

                return EvaluatedMove.strategic(
                    detection.move(),
                    score,
                    9.5,
                    safety,
                    risk,
                    "Fourchette sur "
                        + detection.targets().size()
                        + " pièce(s), valeur totale "
                        + detection.targetValueSum()
                );
            })
            .toList();
    }

    /**
     * Évalue les nouveaux clouages selon la valeur de la pièce immobilisée et
     * la sécurité de l'attaquant.
     */
    public static Action<PinDetection> playBestPin() {
        return (context, detections) -> detections.stream()
            .map(detection -> {
                double safety = !detection.attackerAttacked()
                    ? 9.0
                    : detection.attackerDefended() ? 6.0 : 2.5;
                double risk = 10.0 - safety;

                double score = Math.clamp(
                    6.0
                        + detection.pinnedValue() * 0.35
                        - (detection.attackerAttacked()
                            && !detection.attackerDefended()
                            ? 1.3
                            : 0.0),
                    0.0,
                    9.4
                );

                return EvaluatedMove.strategic(
                    detection.move(),
                    score,
                    7.5,
                    safety,
                    risk,
                    "Clouage absolu de "
                        + detection.pattern().pinned().piece().type()
                        + " devant le roi"
                );
            })
            .toList();
    }

    /**
     * Évalue les enfilades selon la cible susceptible d'être gagnée derrière
     * la pièce de forte valeur.
     */
    public static Action<SkewerDetection> playBestSkewer() {
        return (context, detections) -> detections.stream()
            .map(detection -> {
                double safety = !detection.attackerAttacked()
                    ? 9.0
                    : detection.attackerDefended() ? 6.0 : 2.5;
                double risk = 10.0 - safety;

                double kingBonus =
                    detection.frontValue() >= 100 ? 1.2 : 0.0;

                double score = Math.clamp(
                    5.8
                        + detection.rearValue() * 0.42
                        + kingBonus
                        - (detection.attackerAttacked()
                            && !detection.attackerDefended()
                            ? 1.3
                            : 0.0),
                    0.0,
                    9.6
                );

                return EvaluatedMove.strategic(
                    detection.move(),
                    score,
                    8.5,
                    safety,
                    risk,
                    "Enfilade : "
                        + detection.pattern().front().piece().type()
                        + " devant "
                        + detection.pattern().rear().piece().type()
                );
            })
            .toList();
    }


    /**
     * Un double échec est extrêmement forçant : seules des réponses du roi
     * sont généralement possibles.
     */
    public static Action<DoubleCheckDetection> playDoubleCheck() {
        return (context, detections) -> detections.stream()
            .map(detection -> EvaluatedMove.strategic(
                detection.move(),
                9.7,
                10.0,
                6.5,
                3.5,
                "Double échec par "
                    + detection.attackers().size()
                    + " attaquants"
            ))
            .toList();
    }

    /**
     * Évalue une attaque à la découverte selon la valeur de la nouvelle cible
     * et la sécurité de la pièce qui s'écarte.
     */
    public static Action<DiscoveredAttackDetection>
        playBestDiscoveredAttack() {

        return (context, detections) -> detections.stream()
            .map(detection -> {
                double safety = !detection.movedPieceAttacked()
                    ? 8.5
                    : detection.movedPieceDefended() ? 6.0 : 2.5;

                double risk = 10.0 - safety;
                double targetBonus =
                    detection.targetValue() >= 100
                        ? 3.0
                        : detection.targetValue() * 0.38;

                double score = Math.clamp(
                    5.8
                        + targetBonus
                        - (detection.movedPieceAttacked()
                            && !detection.movedPieceDefended()
                            ? 1.2
                            : 0.0),
                    0.0,
                    9.7
                );

                return EvaluatedMove.strategic(
                    detection.move(),
                    score,
                    9.0,
                    safety,
                    risk,
                    "Attaque à la découverte : "
                        + detection.revealedAttacker().piece().type()
                        + " révèle une attaque sur "
                        + detection.target().piece().type()
                );
            })
            .toList();
    }


    /**
     * Évalue la capture d'un défenseur surchargé selon la valeur de la pièce
     * capturée et surtout la valeur des cibles réellement exposées ensuite.
     */
    public static Action<RemoveDefenderDetection>
        removeOverloadedDefender() {

        return (context, detections) -> detections.stream()
            .map(detection -> {
                PositionProjection projection =
                    context.analysis().after(detection.move());

                Optional<Piece> movedPiece =
                    projection.position().pieceAt(
                        detection.move().to()
                    );

                boolean attacked = false;
                boolean defended = false;

                if (movedPiece.isPresent()) {
                    PlacedPiece placed = new PlacedPiece(
                        movedPiece.orElseThrow(),
                        detection.move().to()
                    );

                    attacked =
                        projection.analysis().isAttacked(placed);
                    defended =
                        projection.analysis().isDefended(placed);
                }

                double safety = !attacked
                    ? 9.0
                    : defended ? 6.0 : 2.5;
                double risk = 10.0 - safety;

                int defenderValue = context.analysis()
                    .pieceValues()
                    .valueOf(
                        detection.overload()
                            .defender()
                            .piece()
                            .type()
                    );

                double score = Math.clamp(
                    6.3
                        + defenderValue * 0.18
                        + detection.exposedValue() * 0.32
                        - (attacked && !defended ? 1.2 : 0.0),
                    0.0,
                    9.7
                );

                return EvaluatedMove.strategic(
                    detection.move(),
                    score,
                    8.8,
                    safety,
                    risk,
                    "Élimination d'un défenseur surchargé : "
                        + detection.newlyHangingTargets().size()
                        + " cible(s) exposée(s), valeur "
                        + detection.exposedValue()
                );
            })
            .toList();
    }


    /**
     * Évalue tous les coups légaux selon la qualité globale de la position
     * obtenue.
     *
     * <p>Cette action constitue le pont entre les heuristiques positionnelles
     * du framework et la mécanique générique d'\{@code EvaluatedMove\}.
     * Chaque coup est simulé puis reçoit une note de 0 à 10.</p>
     */
    public static <D extends Detection> Action<D> bestPosition() {
        return (context, detections) -> context.legalMoves().stream()
            .map(move -> {
                PositionProjection projection =
                    context.analysis().after(move);

                var evaluation = projection.analysis()
                    .positionEvaluation(context.myColor());

                double aggression = Math.clamp(
                    (
                        evaluation.mobility()
                            + evaluation.centerControl()
                    ) / 2.0,
                    0.0,
                    10.0
                );

                double safety = evaluation.kingSafety();
                double risk = 10.0 - safety;

                return EvaluatedMove.strategic(
                    move,
                    evaluation.total(),
                    aggression,
                    safety,
                    risk,
                    evaluation.explanation()
                );
            })
            .toList();
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
