package fr.astroware.chess.bot.situation;

import fr.astroware.chess.bot.analysis.Analysis;
import fr.astroware.chess.bot.analysis.PinPattern;
import fr.astroware.chess.bot.analysis.PositionProjection;
import fr.astroware.chess.bot.analysis.SkewerPattern;
import fr.astroware.chess.bot.rule.PresenceDetection;
import fr.astroware.chess.bot.rule.Situation;
import fr.astroware.chess.bot.situation.detection.CaptureDetection;
import fr.astroware.chess.bot.situation.detection.CheckingMoveDetection;
import fr.astroware.chess.bot.situation.detection.ForkDetection;
import fr.astroware.chess.bot.situation.detection.MateInOneDetection;
import fr.astroware.chess.bot.situation.detection.PinDetection;
import fr.astroware.chess.bot.situation.detection.SkewerDetection;
import fr.astroware.chess.bot.situation.detection.ThreatenedPieceDetection;
import fr.astroware.chess.core.game.GameStatus;
import fr.astroware.chess.core.model.Color;
import fr.astroware.chess.core.model.Move;
import fr.astroware.chess.core.model.Piece;
import fr.astroware.chess.core.model.PieceType;
import fr.astroware.chess.core.model.PlacedPiece;

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

    public static Situation<CaptureDetection> hangingEnemyPiece() {
        return captureAvailable().filter(CaptureDetection::targetIsHanging);
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
     * Détecte tous les coups qui terminent immédiatement la partie par mat.
     */
    public static Situation<MateInOneDetection> mateInOne() {
        return context -> {
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

    private static int tacticalValue(
        PieceType type,
        Analysis analysis
    ) {
        return type == PieceType.KING
            ? 100
            : analysis.pieceValues().valueOf(type);
    }
}
