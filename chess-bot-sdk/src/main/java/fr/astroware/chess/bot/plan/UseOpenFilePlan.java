package fr.astroware.chess.bot.plan;

import fr.astroware.chess.bot.analysis.FileStatus;
import fr.astroware.chess.bot.analysis.PositionProjection;
import fr.astroware.chess.bot.api.BotContext;
import fr.astroware.chess.bot.evaluation.EvaluatedMove;
import fr.astroware.chess.core.model.Color;
import fr.astroware.chess.core.model.Move;
import fr.astroware.chess.core.model.PieceType;
import fr.astroware.chess.core.model.PlacedPiece;

import java.util.List;
import java.util.Optional;

/**
 * Plan visant à installer une tour sur une colonne ouverte ou semi-ouverte.
 */
final class UseOpenFilePlan implements StrategicPlan {

    @Override
    public String name() {
        return "Occuper une colonne ouverte";
    }

    @Override
    public String description() {
        return "Activer une tour sur une colonne sans pion allié.";
    }

    @Override
    public PlanProgress progress(BotContext context) {
        boolean alreadyActive =
            context.position().pieces(context.myColor()).stream()
                .filter(piece ->
                    piece.piece().type() == PieceType.ROOK
                )
                .anyMatch(piece ->
                    favorable(
                        context.analysis().fileStatus(
                            piece.square().file()
                        ),
                        context.myColor()
                    )
                );

        if (alreadyActive) {
            return PlanProgress.completed(
                "Une tour occupe déjà une colonne ouverte ou semi-ouverte."
            );
        }

        return PlanProgress.active(
            4.0,
            "Aucune tour n'est encore placée sur une colonne favorable."
        );
    }

    @Override
    public List<EvaluatedMove> candidates(BotContext context) {
        return context.legalMoves().stream()
            .filter(move ->
                context.position()
                    .pieceAt(move.from())
                    .filter(piece ->
                        piece.color() == context.myColor()
                            && piece.type() == PieceType.ROOK
                    )
                    .isPresent()
            )
            .map(move -> candidate(context, move))
            .flatMap(Optional::stream)
            .toList();
    }

    private Optional<EvaluatedMove> candidate(
        BotContext context,
        Move move
    ) {
        PositionProjection projection =
            context.analysis().after(move);

        FileStatus status =
            projection.analysis().fileStatus(
                move.to().file()
            );

        if (!favorable(status, context.myColor())) {
            return Optional.empty();
        }

        double base = status == FileStatus.OPEN
            ? 8.3
            : 7.5;

        double mobility =
            projection.analysis().mobilityScore(
                context.myColor()
            );

        double score = Math.clamp(
            base + (mobility - 5.0) * 0.12,
            0.0,
            9.4
        );

        var movedPiece =
            projection.position().pieceAt(move.to());

        double safety = 7.0;

        if (movedPiece.isPresent()) {
            PlacedPiece rook = new PlacedPiece(
                movedPiece.orElseThrow(),
                move.to()
            );

            boolean attacked =
                projection.analysis().isAttacked(rook);
            boolean defended =
                projection.analysis().isDefended(rook);

            safety = !attacked
                ? 9.0
                : defended ? 6.0 : 2.5;
        }

        return Optional.of(
            EvaluatedMove.strategic(
                move,
                score,
                6.5,
                safety,
                10.0 - safety,
                status == FileStatus.OPEN
                    ? "Place une tour sur une colonne ouverte"
                    : "Place une tour sur une colonne semi-ouverte"
            )
        );
    }

    private static boolean favorable(
        FileStatus status,
        Color color
    ) {
        return status == FileStatus.OPEN
            || (color == Color.WHITE
                && status == FileStatus.WHITE_SEMI_OPEN)
            || (color == Color.BLACK
                && status == FileStatus.BLACK_SEMI_OPEN);
    }
}
