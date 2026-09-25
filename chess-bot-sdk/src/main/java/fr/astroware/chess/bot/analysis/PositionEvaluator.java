package fr.astroware.chess.bot.analysis;

import fr.astroware.chess.core.model.BoardFile;
import fr.astroware.chess.core.model.BoardRank;
import fr.astroware.chess.core.model.Color;
import fr.astroware.chess.core.model.PieceType;
import fr.astroware.chess.core.model.PlacedPiece;
import fr.astroware.chess.core.model.PositionView;
import fr.astroware.chess.core.model.Square;

import java.util.List;
import java.util.Optional;

/**
 * Évaluation positionnelle pédagogique et explicable.
 */
final class PositionEvaluator {

    private static final List<Square> CENTER = List.of(
        Square.from("d4"),
        Square.from("e4"),
        Square.from("d5"),
        Square.from("e5")
    );

    private PositionEvaluator() {
    }

    static PositionEvaluation evaluate(
        PositionView position,
        AttackMap attackMap,
        PieceValues pieceValues,
        GamePhase phase,
        Color perspective
    ) {
        double material = materialScore(
            position,
            pieceValues,
            perspective
        );

        double mobility = mobilityScore(
            attackMap,
            perspective
        );

        double center = centerControlScore(
            position,
            attackMap,
            perspective
        );

        PawnStructure pawnStructure =
            PawnStructureAnalyzer.analyze(
                position,
                attackMap,
                perspective
            );

        double pawns = pawnStructureScore(pawnStructure);

        double king = kingSafetyScore(
            position,
            attackMap,
            perspective,
            phase
        );

        Weights weights = weightsFor(phase);

        double total = clamp(
            material * weights.material()
                + mobility * weights.mobility()
                + center * weights.center()
                + pawns * weights.pawns()
                + king * weights.king()
        );

        String explanation =
            "matériel=" + oneDecimal(material)
                + ", mobilité=" + oneDecimal(mobility)
                + ", centre=" + oneDecimal(center)
                + ", pions=" + oneDecimal(pawns)
                + ", roi=" + oneDecimal(king)
                + ", phase=" + phase;

        return new PositionEvaluation(
            perspective,
            total,
            material,
            mobility,
            center,
            pawns,
            king,
            explanation
        );
    }

    static double mobilityScore(
        AttackMap attackMap,
        Color color
    ) {
        int mine = attackMap.attackedBy(color).size();
        int theirs = attackMap.attackedBy(color.opposite()).size();

        return relativeScore(mine - theirs, 4.0);
    }

    static double centerControlScore(
        PositionView position,
        AttackMap attackMap,
        Color color
    ) {
        double mine = 0.0;
        double theirs = 0.0;

        for (Square square : CENTER) {
            mine += attackMap.attackCount(square, color);
            theirs += attackMap.attackCount(
                square,
                color.opposite()
            );

            var occupant = position.pieceAt(square);

            if (occupant.isPresent()) {
                if (occupant.orElseThrow().color() == color) {
                    mine += 1.5;
                } else {
                    theirs += 1.5;
                }
            }
        }

        return relativeScore(mine - theirs, 1.2);
    }

    static double pawnStructureScore(PawnStructure structure) {
        double score = 5.0;

        score -= structure.isolatedCount() * 0.55;
        score -= structure.doubledCount() * 0.35;
        score += structure.passedCount() * 0.65;
        score += structure.protectedPassed().size() * 0.35;

        return clamp(score);
    }

    static double kingSafetyScore(
        PositionView position,
        AttackMap attackMap,
        Color color,
        GamePhase phase
    ) {
        Optional<PlacedPiece> king = position.pieces(color)
            .stream()
            .filter(piece ->
                piece.piece().type() == PieceType.KING
            )
            .findFirst();

        if (king.isEmpty()) {
            return 0.0;
        }

        Square kingSquare = king.orElseThrow().square();
        Color opponent = color.opposite();

        double danger = 0.0;
        double shield = 0.0;

        for (int fileDelta = -1; fileDelta <= 1; fileDelta++) {
            for (int rankDelta = -1; rankDelta <= 1; rankDelta++) {
                if (fileDelta == 0 && rankDelta == 0) {
                    continue;
                }

                Optional<Square> nearby =
                    offset(
                        kingSquare,
                        fileDelta,
                        rankDelta
                    );

                if (nearby.isEmpty()) {
                    continue;
                }

                Square square = nearby.orElseThrow();

                danger += attackMap.attackCount(
                    square,
                    opponent
                ) * 0.45;

                shield += position.pieceAt(square)
                    .filter(piece ->
                        piece.color() == color
                            && piece.type() == PieceType.PAWN
                    )
                    .isPresent()
                    ? 0.55
                    : 0.0;
            }
        }

        danger += attackMap.attackCount(
            kingSquare,
            opponent
        ) * 1.2;

        double phaseShieldWeight =
            phase == GamePhase.ENDGAME ? 0.35 : 1.0;

        return clamp(
            7.0
                + shield * phaseShieldWeight
                - danger
        );
    }

    static FileStatus fileStatus(
        PositionView position,
        BoardFile file
    ) {
        boolean whitePawn = hasPawnOnFile(
            position,
            Color.WHITE,
            file
        );
        boolean blackPawn = hasPawnOnFile(
            position,
            Color.BLACK,
            file
        );

        if (!whitePawn && !blackPawn) {
            return FileStatus.OPEN;
        }

        if (!whitePawn) {
            return FileStatus.WHITE_SEMI_OPEN;
        }

        if (!blackPawn) {
            return FileStatus.BLACK_SEMI_OPEN;
        }

        return FileStatus.CLOSED;
    }

    private static boolean hasPawnOnFile(
        PositionView position,
        Color color,
        BoardFile file
    ) {
        return position.pieces(color).stream()
            .anyMatch(piece ->
                piece.piece().type() == PieceType.PAWN
                    && piece.square().file() == file
            );
    }

    private static double materialScore(
        PositionView position,
        PieceValues values,
        Color color
    ) {
        int mine = position.pieces(color).stream()
            .map(PlacedPiece::piece)
            .map(piece -> piece.type())
            .mapToInt(values::valueOf)
            .sum();

        int theirs = position.pieces(color.opposite()).stream()
            .map(PlacedPiece::piece)
            .map(piece -> piece.type())
            .mapToInt(values::valueOf)
            .sum();

        return relativeScore(mine - theirs, 0.55);
    }

    private static double relativeScore(
        double difference,
        double scale
    ) {
        return clamp(5.0 + difference * scale);
    }

    private static Weights weightsFor(GamePhase phase) {
        return switch (phase) {
            case OPENING ->
                new Weights(0.20, 0.20, 0.20, 0.15, 0.25);
            case MIDDLEGAME ->
                new Weights(0.27, 0.20, 0.16, 0.15, 0.22);
            case ENDGAME ->
                new Weights(0.30, 0.20, 0.05, 0.30, 0.15);
        };
    }

    private static Optional<Square> offset(
        Square origin,
        int fileDelta,
        int rankDelta
    ) {
        int fileIndex =
            origin.file().ordinal() + fileDelta;
        int rankNumber =
            origin.rank().number() + rankDelta;

        if (fileIndex < 0
            || fileIndex >= BoardFile.values().length
            || rankNumber < 1
            || rankNumber > 8) {
            return Optional.empty();
        }

        return Optional.of(
            new Square(
                BoardFile.values()[fileIndex],
                BoardRank.from(rankNumber)
            )
        );
    }

    private static double clamp(double value) {
        return Math.clamp(value, 0.0, 10.0);
    }

    private static String oneDecimal(double value) {
        return String.format(java.util.Locale.ROOT, "%.1f", value);
    }

    private record Weights(
        double material,
        double mobility,
        double center,
        double pawns,
        double king
    ) {
    }
}
