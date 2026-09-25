package fr.astroware.chess.bot.plan;

import fr.astroware.chess.bot.api.BotContext;
import fr.astroware.chess.bot.evaluation.EvaluatedMove;
import fr.astroware.chess.core.model.Color;
import fr.astroware.chess.core.model.Move;
import fr.astroware.chess.core.model.Square;

import java.util.Set;

/**
 * Plan de développement des cavaliers et des fous depuis leurs cases initiales.
 */
final class DevelopMinorPiecesPlan implements StrategicPlan {

    @Override
    public String name() {
        return "Développer les pièces mineures";
    }

    @Override
    public String description() {
        return "Faire sortir les cavaliers et les fous de leurs cases initiales.";
    }

    @Override
    public PlanProgress progress(BotContext context) {
        Set<Square> starts = startingSquares(context.myColor());

        long remaining = starts.stream()
            .filter(square -> context.position().pieceAt(square)
                .filter(piece -> piece.color() == context.myColor())
                .isPresent())
            .count();

        if (remaining == 0) {
            return PlanProgress.completed(
                "Les pièces mineures ont quitté leurs cases initiales."
            );
        }

        double completion = (4.0 - remaining) / 4.0 * 10.0;

        return PlanProgress.active(
            completion,
            remaining + " pièce(s) mineure(s) restent à développer."
        );
    }

    @Override
    public java.util.List<EvaluatedMove> candidates(BotContext context) {
        Set<Square> starts = startingSquares(context.myColor());

        return context.legalMoves().stream()
            .filter(move -> starts.contains(move.from()))
            .map(move -> EvaluatedMove.strategic(
                move,
                7.0,
                5.0,
                7.0,
                3.0,
                "Développement d'une pièce mineure."
            ))
            .toList();
    }

    private static Set<Square> startingSquares(Color color) {
        return color == Color.WHITE
            ? Set.of(
                Square.from("b1"),
                Square.from("g1"),
                Square.from("c1"),
                Square.from("f1")
            )
            : Set.of(
                Square.from("b8"),
                Square.from("g8"),
                Square.from("c8"),
                Square.from("f8")
            );
    }
}
