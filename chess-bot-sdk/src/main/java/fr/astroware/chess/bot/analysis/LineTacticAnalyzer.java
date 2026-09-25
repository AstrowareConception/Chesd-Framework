package fr.astroware.chess.bot.analysis;

import fr.astroware.chess.core.model.BoardFile;
import fr.astroware.chess.core.model.BoardRank;
import fr.astroware.chess.core.model.Color;
import fr.astroware.chess.core.model.PieceType;
import fr.astroware.chess.core.model.PlacedPiece;
import fr.astroware.chess.core.model.PositionView;
import fr.astroware.chess.core.model.Square;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Analyse géométrique des tactiques de ligne : clouages et enfilades.
 */
final class LineTacticAnalyzer {

    private static final int[][] BISHOP_DIRECTIONS = {
        {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
    };

    private static final int[][] ROOK_DIRECTIONS = {
        {1, 0}, {-1, 0}, {0, 1}, {0, -1}
    };

    private LineTacticAnalyzer() {
    }

    static List<PinPattern> pinsBy(
        PositionView position,
        Color attackerColor
    ) {
        List<PinPattern> patterns = new ArrayList<>();

        for (PlacedPiece attacker : position.pieces(attackerColor)) {
            for (int[] direction : directions(attacker.piece().type())) {
                List<PlacedPiece> encountered =
                    firstPiecesOnRay(position, attacker.square(), direction, 2);

                if (encountered.size() != 2) {
                    continue;
                }

                PlacedPiece first = encountered.get(0);
                PlacedPiece second = encountered.get(1);

                if (first.piece().color() == attackerColor
                    || second.piece().color() == attackerColor) {
                    continue;
                }

                if (first.piece().type() == PieceType.KING) {
                    continue;
                }

                if (second.piece().type() == PieceType.KING) {
                    patterns.add(
                        new PinPattern(attacker, first, second)
                    );
                }
            }
        }

        return List.copyOf(patterns);
    }

    static List<SkewerPattern> skewersBy(
        PositionView position,
        Color attackerColor,
        PieceValues values
    ) {
        List<SkewerPattern> patterns = new ArrayList<>();

        for (PlacedPiece attacker : position.pieces(attackerColor)) {
            for (int[] direction : directions(attacker.piece().type())) {
                List<PlacedPiece> encountered =
                    firstPiecesOnRay(position, attacker.square(), direction, 2);

                if (encountered.size() != 2) {
                    continue;
                }

                PlacedPiece front = encountered.get(0);
                PlacedPiece rear = encountered.get(1);

                if (front.piece().color() == attackerColor
                    || rear.piece().color() == attackerColor) {
                    continue;
                }

                int frontValue = tacticalValue(front, values);
                int rearValue = tacticalValue(rear, values);

                if (frontValue > rearValue) {
                    patterns.add(
                        new SkewerPattern(attacker, front, rear)
                    );
                }
            }
        }

        return List.copyOf(patterns);
    }

    private static int tacticalValue(
        PlacedPiece piece,
        PieceValues values
    ) {
        return piece.piece().type() == PieceType.KING
            ? 100
            : values.valueOf(piece.piece().type());
    }

    private static int[][] directions(PieceType type) {
        return switch (type) {
            case BISHOP -> BISHOP_DIRECTIONS;
            case ROOK -> ROOK_DIRECTIONS;
            case QUEEN -> new int[][] {
                {1, 1}, {1, -1}, {-1, 1}, {-1, -1},
                {1, 0}, {-1, 0}, {0, 1}, {0, -1}
            };
            case PAWN, KNIGHT, KING -> new int[0][];
        };
    }

    private static List<PlacedPiece> firstPiecesOnRay(
        PositionView position,
        Square origin,
        int[] direction,
        int limit
    ) {
        List<PlacedPiece> encountered = new ArrayList<>();
        Square current = origin;

        while (encountered.size() < limit) {
            Optional<Square> next = offset(
                current,
                direction[0],
                direction[1]
            );

            if (next.isEmpty()) {
                break;
            }

            Square square = next.orElseThrow();

            position.pieceAt(square).ifPresent(piece ->
                encountered.add(new PlacedPiece(piece, square))
            );

            current = square;
        }

        return encountered;
    }

    private static Optional<Square> offset(
        Square origin,
        int fileDelta,
        int rankDelta
    ) {
        int fileIndex = origin.file().ordinal() + fileDelta;
        int rankNumber = origin.rank().number() + rankDelta;

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
}
