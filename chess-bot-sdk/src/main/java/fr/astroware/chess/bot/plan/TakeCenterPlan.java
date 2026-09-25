package fr.astroware.chess.bot.plan;

import fr.astroware.chess.bot.api.BotContext;
import fr.astroware.chess.bot.evaluation.EvaluatedMove;
import fr.astroware.chess.core.model.Color;
import fr.astroware.chess.core.model.Move;
import fr.astroware.chess.core.model.Piece;
import fr.astroware.chess.core.model.PieceType;
import fr.astroware.chess.core.model.Square;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Plan pédagogique consistant à occuper progressivement le centre.
 *
 * <p>Cette première version travaille sur l'occupation des quatre cases
 * centrales. Lorsque l'AttackMap sera disponible, le plan pourra également
 * mesurer leur contrôle sans changer son API publique.</p>
 */
final class TakeCenterPlan implements StrategicPlan {

    private static final Set<Square> CENTER = Set.of(
        Square.from("d4"),
        Square.from("e4"),
        Square.from("d5"),
        Square.from("e5")
    );

    @Override
    public String name() {
        return "Prendre le centre";
    }

    @Override
    public String description() {
        return "Occuper progressivement les cases centrales avec des pièces actives.";
    }

    @Override
    public PlanProgress progress(BotContext context) {
        long occupied = CENTER.stream()
            .map(context.position()::pieceAt)
            .flatMap(Optional::stream)
            .filter(piece -> piece.color() == context.myColor())
            .count();

        if (occupied >= 2) {
            return PlanProgress.completed(
                "Au moins deux cases du centre sont occupées par nos pièces."
            );
        }

        return PlanProgress.active(
            occupied * 5.0,
            occupied == 0
                ? "Le centre n'est pas encore occupé."
                : "Une case centrale est déjà occupée."
        );
    }

    @Override
    public List<EvaluatedMove> candidates(BotContext context) {
        if (progress(context).state() == PlanState.COMPLETED) {
            return List.of();
        }

        return context.legalMoves().stream()
            .filter(move -> CENTER.contains(move.to()))
            .map(move -> evaluate(context, move))
            .toList();
    }

    private EvaluatedMove evaluate(BotContext context, Move move) {
        Optional<Piece> movingPiece = context.position().pieceAt(move.from());
        boolean pawnMove = movingPiece
            .map(Piece::type)
            .filter(type -> type == PieceType.PAWN)
            .isPresent();

        return EvaluatedMove.strategic(
            move,
            pawnMove ? 8.0 : 7.5,
            6.0,
            pawnMove ? 6.0 : 5.0,
            4.0,
            "Ce coup contribue au plan « Prendre le centre »."
        );
    }
}
