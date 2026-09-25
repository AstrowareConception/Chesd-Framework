package fr.astroware.chess.bot.analysis;

import fr.astroware.chess.core.model.Color;
import fr.astroware.chess.core.model.PieceType;
import fr.astroware.chess.core.model.PlacedPiece;
import fr.astroware.chess.core.model.PositionView;

import java.util.ArrayList;
import java.util.List;

/**
 * Analyse déterministe de la structure de pions.
 */
final class PawnStructureAnalyzer {

    private PawnStructureAnalyzer() {
    }

    static PawnStructure analyze(
        PositionView position,
        AttackMap attackMap,
        Color color
    ) {
        List<PlacedPiece> pawns = position.pieces(color).stream()
            .filter(piece -> piece.piece().type() == PieceType.PAWN)
            .toList();

        List<PlacedPiece> isolated = pawns.stream()
            .filter(pawn -> isIsolated(pawn, pawns))
            .toList();

        List<PlacedPiece> doubled = pawns.stream()
            .filter(pawn -> pawns.stream()
                .filter(other -> !other.equals(pawn))
                .anyMatch(other ->
                    other.square().file() == pawn.square().file()
                ))
            .toList();

        List<PlacedPiece> enemyPawns =
            position.pieces(color.opposite()).stream()
                .filter(piece ->
                    piece.piece().type() == PieceType.PAWN
                )
                .toList();

        List<PlacedPiece> passed = pawns.stream()
            .filter(pawn -> isPassed(pawn, enemyPawns))
            .toList();

        List<PlacedPiece> protectedPassed = passed.stream()
            .filter(pawn -> attackMap
                .attackersOf(pawn.square(), color)
                .stream()
                .anyMatch(defender ->
                    defender.piece().type() == PieceType.PAWN
                        && !defender.equals(pawn)
                ))
            .toList();

        return new PawnStructure(
            color,
            pawns,
            isolated,
            doubled,
            passed,
            protectedPassed
        );
    }

    private static boolean isIsolated(
        PlacedPiece pawn,
        List<PlacedPiece> friendlyPawns
    ) {
        int file = pawn.square().file().ordinal();

        return friendlyPawns.stream()
            .filter(other -> !other.equals(pawn))
            .noneMatch(other ->
                Math.abs(
                    other.square().file().ordinal() - file
                ) == 1
            );
    }

    private static boolean isPassed(
        PlacedPiece pawn,
        List<PlacedPiece> enemyPawns
    ) {
        int file = pawn.square().file().ordinal();
        int rank = pawn.square().rank().number();
        int direction =
            pawn.piece().color() == Color.WHITE ? 1 : -1;

        return enemyPawns.stream().noneMatch(enemy -> {
            int enemyFile = enemy.square().file().ordinal();
            int enemyRank = enemy.square().rank().number();

            boolean relevantFile =
                Math.abs(enemyFile - file) <= 1;

            boolean ahead =
                (enemyRank - rank) * direction > 0;

            return relevantFile && ahead;
        });
    }
}
