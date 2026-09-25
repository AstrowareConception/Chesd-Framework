package fr.astroware.chess.bot.situation;

import fr.astroware.chess.bot.analysis.Analysis;
import fr.astroware.chess.bot.analysis.BatteryPattern;
import fr.astroware.chess.bot.analysis.GamePhase;
import fr.astroware.chess.bot.analysis.OverloadedDefenderPattern;
import fr.astroware.chess.bot.analysis.PinPattern;
import fr.astroware.chess.bot.analysis.PositionProjection;
import fr.astroware.chess.bot.analysis.SkewerPattern;
import fr.astroware.chess.bot.analysis.XRayPattern;
import fr.astroware.chess.bot.rule.PresenceDetection;
import fr.astroware.chess.bot.rule.Situation;
import fr.astroware.chess.bot.situation.detection.CaptureDetection;
import fr.astroware.chess.bot.situation.detection.CastlingDetection;
import fr.astroware.chess.bot.situation.detection.CenterImprovementDetection;
import fr.astroware.chess.bot.situation.detection.DiscoveredAttackDetection;
import fr.astroware.chess.bot.situation.detection.DevelopmentDetection;
import fr.astroware.chess.bot.situation.detection.DeflectionDetection;
import fr.astroware.chess.bot.situation.detection.DoubleCheckDetection;
import fr.astroware.chess.bot.situation.detection.CheckingMoveDetection;
import fr.astroware.chess.bot.situation.detection.ForkDetection;
import fr.astroware.chess.bot.situation.detection.MateInOneDetection;
import fr.astroware.chess.bot.situation.detection.MateRiskDetection;
import fr.astroware.chess.bot.situation.detection.PinDetection;
import fr.astroware.chess.bot.situation.detection.PromotionDetection;
import fr.astroware.chess.bot.situation.detection.RemoveDefenderDetection;
import fr.astroware.chess.bot.situation.detection.SkewerDetection;
import fr.astroware.chess.bot.situation.detection.ThreatenedPieceDetection;
import fr.astroware.chess.bot.situation.detection.XRayDetection;
import fr.astroware.chess.core.game.GameStatus;
import fr.astroware.chess.core.model.Color;
import fr.astroware.chess.core.model.Move;
import fr.astroware.chess.core.model.Piece;
import fr.astroware.chess.core.model.PieceType;
import fr.astroware.chess.core.model.PlacedPiece;
import fr.astroware.chess.core.model.Square;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Point d'entrée vers les situations fournies par le framework.
 */
public final class Situations {

    private Situations() {
    }


    /**
     * Situation binaire reconnue uniquement dans la phase demandée.
     */
    public static Situation<PresenceDetection> inPhase(GamePhase phase) {
        return context -> context.analysis().gamePhase() == phase
            ? List.of(PresenceDetection.INSTANCE)
            : List.of();
    }

    /**
     * Garde une situation typée derrière une phase de jeu.
     *
     * <p>Le type de détection est conservé, ce qui permet d'associer la même
     * Action qu'à la situation d'origine.</p>
     */
    public static <D extends fr.astroware.chess.bot.rule.Detection>
        Situation<D> onlyInPhase(
            GamePhase phase,
            Situation<D> situation
        ) {

        java.util.Objects.requireNonNull(
            phase,
            "phase must not be null"
        );
        java.util.Objects.requireNonNull(
            situation,
            "situation must not be null"
        );

        return situation.when(
            context -> context.analysis().gamePhase() == phase
        );
    }

    public static Situation<PresenceDetection> always() {
        return context -> List.of(PresenceDetection.INSTANCE);
    }

    /**
     * Situation reconnue lorsque le roi du bot est actuellement en échec.
     */
    public static Situation<PresenceDetection> inCheck() {
        return context -> context.analysis().isKingAttacked()
            ? List.of(PresenceDetection.INSTANCE)
            : List.of();
    }

    /**
     * Détecte toutes les captures légales dont la destination contient une
     * pièce adverse.
     */
    public static Situation<CaptureDetection> captureAvailable() {
        return context -> {
            Analysis analysis = context.analysis();
            Color opponent = context.myColor().opposite();
            List<CaptureDetection> detections = new ArrayList<>();

            for (Move move : analysis.captures()) {
                Optional<Piece> attackerPiece =
                    context.position().pieceAt(move.from());
                Optional<Piece> targetPiece =
                    context.position().pieceAt(move.to());

                if (attackerPiece.isEmpty() || targetPiece.isEmpty()) {
                    continue;
                }

                Piece attacker = attackerPiece.orElseThrow();
                Piece target = targetPiece.orElseThrow();

                if (attacker.color() != context.myColor()
                    || target.color() != opponent) {
                    continue;
                }

                PlacedPiece attackerPlaced =
                    new PlacedPiece(attacker, move.from());
                PlacedPiece targetPlaced =
                    new PlacedPiece(target, move.to());

                detections.add(
                    new CaptureDetection(
                        move,
                        attackerPlaced,
                        targetPlaced,
                        analysis.pieceValues().valueOf(attacker.type()),
                        analysis.pieceValues().valueOf(target.type()),
                        analysis.attackersOf(move.to(), opponent).size()
                    )
                );
            }

            return List.copyOf(detections);
        };
    }


    /**
     * Détecte tous les coups légaux réalisant une promotion.
     */
    public static Situation<PromotionDetection>
        promotionAvailable() {

        return context -> context.legalMoves()
            .stream()
            .filter(move -> move.promotion().isPresent())
            .map(move ->
                new PromotionDetection(
                    move,
                    move.promotion().orElseThrow()
                )
            )
            .toList();
    }

    /**
     * Détecte les roques actuellement légaux.
     *
     * <p>Le moteur de règles ayant déjà filtré les coups illégaux, un
     * déplacement horizontal du roi de deux colonnes correspond ici à un
     * roque jouable.</p>
     */
    public static Situation<CastlingDetection>
        castlingAvailable() {

        return context -> context.legalMoves()
            .stream()
            .filter(move ->
                context.position()
                    .pieceAt(move.from())
                    .filter(piece ->
                        piece.color()
                            == context.myColor()
                    )
                    .filter(piece ->
                        piece.type()
                            == PieceType.KING
                    )
                    .isPresent()
            )
            .filter(move ->
                Math.abs(
                    move.to().file().ordinal()
                        - move.from()
                            .file()
                            .ordinal()
                ) == 2
            )
            .map(move ->
                new CastlingDetection(
                    move,
                    move.to().file().ordinal()
                        > move.from()
                            .file()
                            .ordinal()
                )
            )
            .toList();
    }

    public static Situation<CaptureDetection> hangingEnemyPiece() {
        return captureAvailable().filter(CaptureDetection::targetIsHanging);
    }


    /**
     * Détecte toutes les pièces du bot actuellement attaquées.
     *
     * <p>Contrairement à {@link #hangingOwnPiece()}, une pièce défendue reste
     * détectée : l'étudiant peut ainsi distinguer « attaquée » de « pendue ».</p>
     */
    public static Situation<ThreatenedPieceDetection>
        attackedOwnPiece() {

        return context -> context.position()
            .pieces(context.myColor())
            .stream()
            .filter(piece ->
                piece.piece().type()
                    != PieceType.KING
            )
            .filter(piece ->
                context.analysis()
                    .isAttacked(piece)
            )
            .map(piece ->
                new ThreatenedPieceDetection(
                    piece,
                    context.analysis()
                        .pieceValues()
                        .valueOf(
                            piece.piece().type()
                        ),
                    context.analysis()
                        .attackersOf(
                            piece.square(),
                            context.myColor()
                                .opposite()
                        )
                        .size(),
                    context.analysis()
                        .defendersOf(piece)
                        .size()
                )
            )
            .toList();
    }

    /**
     * Détecte les pièces attaquées dont le nombre de défenseurs est
     * strictement inférieur au nombre d'attaquants.
     */
    public static Situation<ThreatenedPieceDetection>
        underDefendedOwnPiece() {

        return attackedOwnPiece().filter(
            detection ->
                detection.defenders()
                    < detection.attackers()
        );
    }

    /**
     * Détecte les coups qui développent un cavalier ou un fou encore présent
     * sur sa case initiale.
     */
    public static Situation<DevelopmentDetection>
        developmentAvailable() {

        return context -> {
            double centerBefore =
                context.analysis()
                    .centerControlScore(
                        context.myColor()
                    );

            double mobilityBefore =
                context.analysis()
                    .mobilityScore(
                        context.myColor()
                    );

            List<DevelopmentDetection> detections =
                new ArrayList<>();

            for (Move move : context.legalMoves()) {
                Optional<Piece> source =
                    context.position()
                        .pieceAt(move.from());

                if (source.isEmpty()
                    || source.orElseThrow().color()
                        != context.myColor()) {
                    continue;
                }

                PieceType type =
                    source.orElseThrow().type();

                if (type != PieceType.KNIGHT
                    && type != PieceType.BISHOP) {
                    continue;
                }

                if (!isInitialMinorPieceSquare(
                    context.myColor(),
                    type,
                    move.from()
                )) {
                    continue;
                }

                PositionProjection projection =
                    context.analysis()
                        .after(move);

                double centerAfter =
                    projection.analysis()
                        .centerControlScore(
                            context.myColor()
                        );

                double mobilityAfter =
                    projection.analysis()
                        .mobilityScore(
                            context.myColor()
                        );

                detections.add(
                    new DevelopmentDetection(
                        move,
                        type,
                        centerAfter - centerBefore,
                        mobilityAfter - mobilityBefore
                    )
                );
            }

            return List.copyOf(detections);
        };
    }

    /**
     * Détecte les coups qui augmentent strictement le score de contrôle du
     * centre du camp du bot.
     */
    public static Situation<CenterImprovementDetection>
        centerImprovementAvailable() {

        return context -> {
            double before =
                context.analysis()
                    .centerControlScore(
                        context.myColor()
                    );

            return context.legalMoves()
                .stream()
                .map(move -> {
                    double after =
                        context.analysis()
                            .after(move)
                            .analysis()
                            .centerControlScore(
                                context.myColor()
                            );

                    return after > before + 1.0e-9
                        ? Optional.of(
                            new CenterImprovementDetection(
                                move,
                                before,
                                after
                            )
                        )
                        : Optional
                            .<CenterImprovementDetection>empty();
                })
                .flatMap(Optional::stream)
                .toList();
        };
    }

    /**
     * Détecte les pièces du bot actuellement attaquées et non défendues.
     */
    public static Situation<ThreatenedPieceDetection> hangingOwnPiece() {
        return context -> {
            Analysis analysis = context.analysis();

            return analysis.hangingPieces(context.myColor()).stream()
                .map(piece -> new ThreatenedPieceDetection(
                    piece,
                    analysis.pieceValues().valueOf(piece.piece().type()),
                    analysis.attackersOf(
                        piece.square(),
                        context.myColor().opposite()
                    ).size(),
                    analysis.defendersOf(piece).size()
                ))
                .toList();
        };
    }


    /**
     * Détecte les coups du bot qui autorisent un mat en un de l'adversaire.
     *
     * <p>La situation est vide lorsqu'aucun coup légal ne donne cette
     * possibilité à l'adversaire.</p>
     */
    public static Situation<MateRiskDetection> mateInOneRisk() {
        return context -> {
            if (context.position().fen() == null
                || context.position().fen().isBlank()) {
                return List.of();
            }

            return context.legalMoves().stream()
            .map(move -> {
                PositionProjection projection =
                    context.analysis().after(move);

                int mateReplies =
                    projection.analysis().mateInOneMoves().size();

                return mateReplies > 0
                    ? Optional.of(
                        new MateRiskDetection(
                            move,
                            mateReplies
                        )
                    )
                    : Optional.<MateRiskDetection>empty();
            })
            .flatMap(Optional::stream)
            .toList();
        };
    }

    /**
     * Détecte tous les coups qui terminent immédiatement la partie par mat.
     */
    public static Situation<MateInOneDetection> mateInOne() {
        return context -> {
            if (context.position().fen() == null
                || context.position().fen().isBlank()) {
                return List.of();
            }

            GameStatus winningStatus = context.myColor() == Color.WHITE
                ? GameStatus.WHITE_WINS
                : GameStatus.BLACK_WINS;

            return context.legalMoves().stream()
                .filter(move ->
                    context.analysis()
                        .after(move)
                        .result()
                        .status() == winningStatus
                )
                .map(MateInOneDetection::new)
                .toList();
        };
    }

    /**
     * Détecte les coups légaux donnant échec.
     */
    public static Situation<CheckingMoveDetection> checkAvailable() {
        return context -> {
            List<CheckingMoveDetection> detections = new ArrayList<>();

            for (Move move : context.legalMoves()) {
                PositionProjection projection =
                    context.analysis().after(move);

                if (!projection.analysis().isKingAttacked()) {
                    continue;
                }

                Optional<Piece> movedPiece =
                    projection.position().pieceAt(move.to());

                boolean attacked = false;
                boolean defended = false;

                if (movedPiece.isPresent()
                    && movedPiece.orElseThrow().color() == context.myColor()) {
                    PlacedPiece placed = new PlacedPiece(
                        movedPiece.orElseThrow(),
                        move.to()
                    );

                    attacked = projection.analysis().isAttacked(placed);
                    defended = projection.analysis().isDefended(placed);
                }

                detections.add(
                    new CheckingMoveDetection(
                        move,
                        projection.legalMoves().size(),
                        attacked,
                        defended
                    )
                );
            }

            return List.copyOf(detections);
        };
    }

    /**
     * Détecte les coups créant une attaque simultanée sur au moins deux
     * pièces adverses.
     */
    public static Situation<ForkDetection> forkOpportunity() {
        return context -> {
            List<ForkDetection> detections = new ArrayList<>();
            Color opponent = context.myColor().opposite();

            for (Move move : context.legalMoves()) {
                PositionProjection projection =
                    context.analysis().after(move);

                Optional<Piece> movedPiece =
                    projection.position().pieceAt(move.to());

                if (movedPiece.isEmpty()
                    || movedPiece.orElseThrow().color() != context.myColor()) {
                    continue;
                }

                PlacedPiece attacker = new PlacedPiece(
                    movedPiece.orElseThrow(),
                    move.to()
                );

                List<PlacedPiece> targets = projection.analysis()
                    .attackMap()
                    .attacksFrom(attacker)
                    .stream()
                    .map(square -> projection.position()
                        .pieceAt(square)
                        .filter(piece -> piece.color() == opponent)
                        .map(piece -> new PlacedPiece(piece, square)))
                    .flatMap(Optional::stream)
                    .toList();

                if (targets.size() < 2) {
                    continue;
                }

                int targetValueSum = targets.stream()
                    .map(PlacedPiece::piece)
                    .map(Piece::type)
                    .mapToInt(projection.analysis().pieceValues()::valueOf)
                    .sum();

                detections.add(
                    new ForkDetection(
                        move,
                        attacker,
                        targets,
                        targetValueSum,
                        projection.analysis().isAttacked(attacker),
                        projection.analysis().isDefended(attacker)
                    )
                );
            }

            return List.copyOf(detections);
        };
    }

    /**
     * Détecte les coups qui créent un nouveau clouage absolu.
     */
    public static Situation<PinDetection> pinOpportunity() {
        return context -> {
            Set<PinPattern> before = new HashSet<>(
                context.analysis().pinsBy(context.myColor())
            );
            List<PinDetection> detections = new ArrayList<>();

            for (Move move : context.legalMoves()) {
                PositionProjection projection =
                    context.analysis().after(move);

                for (PinPattern pattern
                    : projection.analysis().pinsBy(context.myColor())) {

                    if (before.contains(pattern)) {
                        continue;
                    }

                    detections.add(
                        new PinDetection(
                            move,
                            pattern,
                            projection.analysis()
                                .pieceValues()
                                .valueOf(pattern.pinned().piece().type()),
                            projection.analysis().isAttacked(
                                pattern.attacker()
                            ),
                            projection.analysis().isDefended(
                                pattern.attacker()
                            )
                        )
                    );
                }
            }

            return List.copyOf(detections);
        };
    }

    /**
     * Détecte les coups qui créent une nouvelle enfilade.
     */
    public static Situation<SkewerDetection> skewerOpportunity() {
        return context -> {
            Set<SkewerPattern> before = new HashSet<>(
                context.analysis().skewersBy(context.myColor())
            );
            List<SkewerDetection> detections = new ArrayList<>();

            for (Move move : context.legalMoves()) {
                PositionProjection projection =
                    context.analysis().after(move);

                for (SkewerPattern pattern
                    : projection.analysis().skewersBy(context.myColor())) {

                    if (before.contains(pattern)) {
                        continue;
                    }

                    detections.add(
                        new SkewerDetection(
                            move,
                            pattern,
                            tacticalValue(
                                pattern.front().piece().type(),
                                projection.analysis()
                            ),
                            tacticalValue(
                                pattern.rear().piece().type(),
                                projection.analysis()
                            ),
                            projection.analysis().isAttacked(
                                pattern.attacker()
                            ),
                            projection.analysis().isDefended(
                                pattern.attacker()
                            )
                        )
                    );
                }
            }

            return List.copyOf(detections);
        };
    }


    /**
     * Détecte les coups créant un double échec.
     *
     * <p>Après projection, le roi adverse doit être attaqué par au moins deux
     * pièces du bot.</p>
     */
    public static Situation<DoubleCheckDetection> doubleCheckOpportunity() {
        return context -> {
            List<DoubleCheckDetection> detections = new ArrayList<>();
            Color opponent = context.myColor().opposite();

            for (Move move : context.legalMoves()) {
                PositionProjection projection =
                    context.analysis().after(move);

                Optional<PlacedPiece> king = projection.position()
                    .pieces(opponent)
                    .stream()
                    .filter(piece ->
                        piece.piece().type() == PieceType.KING
                    )
                    .findFirst();

                if (king.isEmpty()) {
                    continue;
                }

                List<PlacedPiece> attackers =
                    projection.analysis().attackersOf(
                        king.orElseThrow().square(),
                        context.myColor()
                    );

                if (attackers.size() >= 2) {
                    detections.add(
                        new DoubleCheckDetection(move, attackers)
                    );
                }
            }

            return List.copyOf(detections);
        };
    }

    /**
     * Détecte une attaque à la découverte.
     *
     * <p>La pièce déplacée doit avoir occupé, avant le coup, une case située
     * entre une pièce coulissante alliée et une nouvelle cible adverse.</p>
     */
    public static Situation<DiscoveredAttackDetection>
        discoveredAttackOpportunity() {

        return context -> {
            List<DiscoveredAttackDetection> detections =
                new ArrayList<>();
            Color attackerColor = context.myColor();
            Color opponent = attackerColor.opposite();

            for (Move move : context.legalMoves()) {
                PositionProjection projection =
                    context.analysis().after(move);

                Optional<Piece> movedPiece =
                    projection.position().pieceAt(move.to());

                boolean movedPieceAttacked = false;
                boolean movedPieceDefended = false;

                if (movedPiece.isPresent()
                    && movedPiece.orElseThrow().color() == attackerColor) {

                    PlacedPiece moved = new PlacedPiece(
                        movedPiece.orElseThrow(),
                        move.to()
                    );

                    movedPieceAttacked =
                        projection.analysis().isAttacked(moved);
                    movedPieceDefended =
                        projection.analysis().isDefended(moved);
                }

                for (PlacedPiece revealed
                    : projection.position().pieces(attackerColor)) {

                    PieceType type = revealed.piece().type();

                    if (!isSlidingPiece(type)
                        || revealed.square().equals(move.to())) {
                        continue;
                    }

                    Optional<Piece> samePieceBefore =
                        context.position().pieceAt(revealed.square());

                    if (samePieceBefore.isEmpty()
                        || !samePieceBefore.orElseThrow()
                            .equals(revealed.piece())) {
                        continue;
                    }

                    PlacedPiece beforeAttacker = new PlacedPiece(
                        samePieceBefore.orElseThrow(),
                        revealed.square()
                    );

                    Set<Square> previouslyAttackedEnemySquares =
                        new HashSet<>();

                    for (Square square : context.analysis()
                        .attackMap()
                        .attacksFrom(beforeAttacker)) {

                        context.position()
                            .pieceAt(square)
                            .filter(piece -> piece.color() == opponent)
                            .ifPresent(piece ->
                                previouslyAttackedEnemySquares.add(square)
                            );
                    }

                    for (Square square : projection.analysis()
                        .attackMap()
                        .attacksFrom(revealed)) {

                        Optional<Piece> targetPiece =
                            projection.position().pieceAt(square);

                        if (targetPiece.isEmpty()
                            || targetPiece.orElseThrow().color()
                                != opponent) {
                            continue;
                        }

                        if (previouslyAttackedEnemySquares.contains(square)) {
                            continue;
                        }

                        if (!isBetweenOnAttackLine(
                            revealed.square(),
                            move.from(),
                            square,
                            type
                        )) {
                            continue;
                        }

                        PlacedPiece target = new PlacedPiece(
                            targetPiece.orElseThrow(),
                            square
                        );

                        detections.add(
                            new DiscoveredAttackDetection(
                                move,
                                revealed,
                                target,
                                tacticalValue(
                                    target.piece().type(),
                                    projection.analysis()
                                ),
                                movedPieceAttacked,
                                movedPieceDefended
                            )
                        );
                    }
                }
            }

            return List.copyOf(detections);
        };
    }



    /**
     * Détecte les coups créant une nouvelle batterie de pièces coulissantes.
     */
    public static Situation<BatteryDetection>
        batteryOpportunity() {

        return context -> {
            Set<BatteryPattern> before =
                new HashSet<>(
                    context.analysis()
                        .batteriesBy(
                            context.myColor()
                        )
                );

            List<BatteryDetection> detections =
                new ArrayList<>();

            for (Move move : context.legalMoves()) {
                PositionProjection projection =
                    context.analysis().after(move);

                for (BatteryPattern pattern
                    : projection.analysis()
                        .batteriesBy(
                            context.myColor()
                        )) {

                    if (before.contains(pattern)) {
                        continue;
                    }

                    detections.add(
                        new BatteryDetection(
                            move,
                            pattern,
                            tacticalValue(
                                pattern.target()
                                    .piece()
                                    .type(),
                                projection.analysis()
                            ),
                            projection.analysis()
                                .isAttacked(
                                    pattern.front()
                                ),
                            projection.analysis()
                                .isDefended(
                                    pattern.front()
                                )
                        )
                    );
                }
            }

            return List.copyOf(detections);
        };
    }

    /**
     * Détecte les coups créant une nouvelle pression de rayon X.
     */
    public static Situation<XRayDetection>
        xRayOpportunity() {

        return context -> {
            Set<XRayPattern> before =
                new HashSet<>(
                    context.analysis()
                        .xRaysBy(
                            context.myColor()
                        )
                );

            List<XRayDetection> detections =
                new ArrayList<>();

            for (Move move : context.legalMoves()) {
                PositionProjection projection =
                    context.analysis().after(move);

                for (XRayPattern pattern
                    : projection.analysis()
                        .xRaysBy(
                            context.myColor()
                        )) {

                    if (before.contains(pattern)) {
                        continue;
                    }

                    detections.add(
                        new XRayDetection(
                            move,
                            pattern,
                            tacticalValue(
                                pattern.blocker()
                                    .piece()
                                    .type(),
                                projection.analysis()
                            ),
                            tacticalValue(
                                pattern.target()
                                    .piece()
                                    .type(),
                                projection.analysis()
                            ),
                            projection.analysis()
                                .isAttacked(
                                    pattern.attacker()
                                ),
                            projection.analysis()
                                .isDefended(
                                    pattern.attacker()
                                )
                        )
                    );
                }
            }

            return List.copyOf(detections);
        };
    }

    /**
     * Détecte une opportunité de déviation : le coup ajoute une nouvelle
     * pression sur le défenseur unique d'une cible plus précieuse.
     *
     * <p>Le motif est volontairement heuristique : il signale une pression
     * exploitable sans prétendre que la séquence suivante est forcée.</p>
     */
    public static Situation<DeflectionDetection>
        deflectionOpportunity() {

        return context -> {
            Color opponent =
                context.myColor().opposite();

            List<DeflectionDetection> detections =
                new ArrayList<>();

            for (PlacedPiece target
                : context.position()
                    .pieces(opponent)) {

                if (target.piece().type()
                    == PieceType.KING) {
                    continue;
                }

                List<PlacedPiece> defenders =
                    context.analysis()
                        .defendersOf(target);

                if (defenders.size() != 1) {
                    continue;
                }

                PlacedPiece defender =
                    defenders.getFirst();

                int defenderValue =
                    tacticalValue(
                        defender.piece().type(),
                        context.analysis()
                    );

                int targetValue =
                    tacticalValue(
                        target.piece().type(),
                        context.analysis()
                    );

                if (targetValue
                    <= defenderValue) {
                    continue;
                }

                int beforeAttackers =
                    context.analysis()
                        .attackersOf(
                            defender.square(),
                            context.myColor()
                        )
                        .size();

                for (Move move
                    : context.legalMoves()) {

                    if (move.to().equals(
                        defender.square()
                    )) {
                        // La capture du défenseur est déjà couverte par
                        // removeOverloadedDefenderOpportunity.
                        continue;
                    }

                    PositionProjection projection =
                        context.analysis()
                            .after(move);

                    Optional<Piece> defenderAfter =
                        projection.position()
                            .pieceAt(
                                defender.square()
                            );

                    Optional<Piece> targetAfter =
                        projection.position()
                            .pieceAt(
                                target.square()
                            );

                    if (defenderAfter.isEmpty()
                        || targetAfter.isEmpty()
                        || defenderAfter
                            .orElseThrow()
                            .color()
                            != opponent
                        || targetAfter
                            .orElseThrow()
                            .color()
                            != opponent) {
                        continue;
                    }

                    PlacedPiece projectedDefender =
                        new PlacedPiece(
                            defenderAfter.orElseThrow(),
                            defender.square()
                        );

                    PlacedPiece projectedTarget =
                        new PlacedPiece(
                            targetAfter.orElseThrow(),
                            target.square()
                        );

                    List<PlacedPiece>
                        projectedDefenders =
                            projection.analysis()
                                .defendersOf(
                                    projectedTarget
                                );

                    if (projectedDefenders.size()
                            != 1
                        || !projectedDefenders
                            .getFirst()
                            .equals(
                                projectedDefender
                            )) {
                        continue;
                    }

                    int afterAttackers =
                        projection.analysis()
                            .attackersOf(
                                defender.square(),
                                context.myColor()
                            )
                            .size();

                    int added =
                        afterAttackers
                            - beforeAttackers;

                    if (added <= 0) {
                        continue;
                    }

                    detections.add(
                        new DeflectionDetection(
                            move,
                            projectedDefender,
                            projectedTarget,
                            defenderValue,
                            targetValue,
                            added
                        )
                    );
                }
            }

            return List.copyOf(detections);
        };
    }

    /**
     * Détecte une attraction du roi par sacrifice : le coup donne échec, le
     * roi peut capturer la pièce offerte, puis le camp attaquant obtient au
     * coup suivant un mat en un ou une capture plus importante que le
     * sacrifice.
     */
    public static Situation<AttractionDetection>
        attractionOpportunity() {

        return context -> {
            Color opponent =
                context.myColor()
                    .opposite();

            List<AttractionDetection> detections =
                new ArrayList<>();

            for (Move move
                : context.legalMoves()) {

                PositionProjection projection =
                    context.analysis()
                        .after(move);

                if (!projection.analysis()
                    .isKingAttacked()) {
                    continue;
                }

                Optional<Piece> offered =
                    projection.position()
                        .pieceAt(move.to());

                if (offered.isEmpty()
                    || offered.orElseThrow()
                        .color()
                        != context.myColor()
                    || offered.orElseThrow()
                        .type()
                        == PieceType.KING) {
                    continue;
                }

                Optional<PlacedPiece> king =
                    projection.position()
                        .pieces(opponent)
                        .stream()
                        .filter(piece ->
                            piece.piece().type()
                                == PieceType.KING
                        )
                        .findFirst();

                if (king.isEmpty()) {
                    continue;
                }

                Optional<Move> kingCapture =
                    projection.legalMoves()
                        .stream()
                        .filter(reply ->
                            reply.from().equals(
                                king.orElseThrow()
                                    .square()
                            )
                        )
                        .filter(reply ->
                            reply.to().equals(
                                move.to()
                            )
                        )
                        .findFirst();

                if (kingCapture.isEmpty()) {
                    continue;
                }

                PositionProjection afterCapture =
                    projection.analysis()
                        .after(
                            kingCapture.orElseThrow()
                        );

                boolean mateFollowUp =
                    !afterCapture.analysis()
                        .mateInOneMoves()
                        .isEmpty();

                int bestCaptureValue =
                    afterCapture.analysis()
                        .captures()
                        .stream()
                        .map(capture ->
                            afterCapture.position()
                                .pieceAt(
                                    capture.to()
                                )
                        )
                        .flatMap(Optional::stream)
                        .map(Piece::type)
                        .mapToInt(type ->
                            tacticalValue(
                                type,
                                afterCapture.analysis()
                            )
                        )
                        .max()
                        .orElse(0);

                int sacrificedValue =
                    tacticalValue(
                        offered.orElseThrow()
                            .type(),
                        projection.analysis()
                    );

                if (!mateFollowUp
                    && bestCaptureValue
                        <= sacrificedValue) {
                    continue;
                }

                detections.add(
                    new AttractionDetection(
                        move,
                        new PlacedPiece(
                            offered.orElseThrow(),
                            move.to()
                        ),
                        king.orElseThrow(),
                        kingCapture.orElseThrow(),
                        sacrificedValue,
                        mateFollowUp,
                        bestCaptureValue
                    )
                );
            }

            return List.copyOf(detections);
        };
    }

    /**
     * Détecte les captures qui éliminent un défenseur surchargé et rendent
     * effectivement une ou plusieurs de ses anciennes cibles pendues.
     */
    public static Situation<RemoveDefenderDetection>
        removeOverloadedDefenderOpportunity() {

        return context -> {
            List<RemoveDefenderDetection> detections =
                new ArrayList<>();

            Color opponent = context.myColor().opposite();
            List<OverloadedDefenderPattern> overloads =
                context.analysis().overloadedDefenders(opponent);

            for (OverloadedDefenderPattern overload : overloads) {
                for (Move move : context.legalMoves()) {
                    if (!move.to().equals(
                        overload.defender().square()
                    )) {
                        continue;
                    }

                    Optional<Piece> captured =
                        context.position().pieceAt(move.to());

                    if (captured.isEmpty()
                        || captured.orElseThrow().color() != opponent) {
                        continue;
                    }

                    PositionProjection projection =
                        context.analysis().after(move);

                    List<PlacedPiece> newlyHanging =
                        overload.protectedTargets().stream()
                            .map(target -> projection.position()
                                .pieceAt(target.square())
                                .filter(piece ->
                                    piece.color() == opponent
                                )
                                .map(piece -> new PlacedPiece(
                                    piece,
                                    target.square()
                                )))
                            .flatMap(Optional::stream)
                            .filter(projection.analysis()::isHanging)
                            .toList();

                    if (newlyHanging.isEmpty()) {
                        continue;
                    }

                    int exposedValue = newlyHanging.stream()
                        .map(PlacedPiece::piece)
                        .map(Piece::type)
                        .mapToInt(
                            projection.analysis()
                                .pieceValues()::valueOf
                        )
                        .sum();

                    detections.add(
                        new RemoveDefenderDetection(
                            move,
                            overload,
                            newlyHanging,
                            exposedValue
                        )
                    );
                }
            }

            return List.copyOf(detections);
        };
    }


    private static boolean isInitialMinorPieceSquare(
        Color color,
        PieceType type,
        Square square
    ) {
        int homeRank =
            color == Color.WHITE ? 1 : 8;

        if (square.rank().number() != homeRank) {
            return false;
        }

        int file =
            square.file().ordinal();

        return switch (type) {
            case KNIGHT -> file == 1 || file == 6;
            case BISHOP -> file == 2 || file == 5;
            default -> false;
        };
    }

    private static boolean isSlidingPiece(PieceType type) {
        return type == PieceType.BISHOP
            || type == PieceType.ROOK
            || type == PieceType.QUEEN;
    }

    private static boolean isBetweenOnAttackLine(
        Square attacker,
        Square blocker,
        Square target,
        PieceType attackerType
    ) {
        int ax = attacker.file().ordinal();
        int ay = attacker.rank().number();
        int bx = blocker.file().ordinal();
        int by = blocker.rank().number();
        int tx = target.file().ordinal();
        int ty = target.rank().number();

        int dx = tx - ax;
        int dy = ty - ay;

        boolean rookLine = dx == 0 || dy == 0;
        boolean bishopLine = Math.abs(dx) == Math.abs(dy);

        boolean allowed = switch (attackerType) {
            case ROOK -> rookLine;
            case BISHOP -> bishopLine;
            case QUEEN -> rookLine || bishopLine;
            case PAWN, KNIGHT, KING -> false;
        };

        if (!allowed || (dx == 0 && dy == 0)) {
            return false;
        }

        int stepX = Integer.signum(dx);
        int stepY = Integer.signum(dy);

        int x = ax + stepX;
        int y = ay + stepY;

        while (x != tx || y != ty) {
            if (x == bx && y == by) {
                return true;
            }

            x += stepX;
            y += stepY;
        }

        return false;
    }

    private static int tacticalValue(
        PieceType type,
        Analysis analysis
    ) {
        return type == PieceType.KING
            ? 100
            : analysis.pieceValues().valueOf(type);
    }
}
