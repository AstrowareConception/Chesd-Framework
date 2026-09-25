package fr.astroware.chess.bot.plan;

import fr.astroware.chess.bot.api.BotContext;
import fr.astroware.chess.bot.evaluation.EvaluatedMove;
import fr.astroware.chess.core.model.Color;
import fr.astroware.chess.core.model.Move;
import fr.astroware.chess.core.model.PieceType;
import fr.astroware.chess.core.model.Square;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Plan adaptatif visant le petit roque.
 *
 * <p>Le plan privilégie le roque lui-même lorsqu'il est légal. Tant qu'il ne
 * l'est pas, il propose plusieurs coups de préparation classiques. Ce n'est
 * donc pas une séquence imposée : le profil du bot peut choisir entre
 * plusieurs routes.</p>
 */
final class CastleKingsidePlan implements StrategicPlan {

    @Override
    public String name() {
        return "Mettre le roi à l'abri";
    }

    @Override
    public String description() {
        return "Préparer puis effectuer le petit roque.";
    }

    @Override
    public PlanProgress progress(BotContext context) {
        Square target = context.myColor() == Color.WHITE
            ? Square.from("g1")
            : Square.from("g8");

        boolean castled = context.position().pieceAt(target)
            .filter(piece -> piece.color() == context.myColor())
            .filter(piece -> piece.type() == PieceType.KING)
            .isPresent();

        if (castled) {
            return PlanProgress.completed(
                "Le roi a atteint la case du petit roque."
            );
        }

        Move castle = castleMove(context.myColor());

        if (context.legalMoves().contains(castle)) {
            return PlanProgress.active(
                9.0,
                "Le petit roque est immédiatement disponible."
            );
        }

        return PlanProgress.active(
            4.0,
            "Le roque n'est pas encore disponible : développement nécessaire."
        );
    }

    @Override
    public List<EvaluatedMove> candidates(BotContext context) {
        Move castle = castleMove(context.myColor());

        if (context.legalMoves().contains(castle)) {
            return List.of(
                EvaluatedMove.strategic(
                    castle,
                    9.5,
                    3.0,
                    10.0,
                    2.0,
                    "Petit roque : objectif principal du plan."
                )
            );
        }

        Map<Move, EvaluatedMove> candidates = new LinkedHashMap<>();

        for (MovePreparation preparation : preparations(context.myColor())) {
            if (context.legalMoves().contains(preparation.move())) {
                candidates.put(
                    preparation.move(),
                    EvaluatedMove.strategic(
                        preparation.move(),
                        preparation.score(),
                        preparation.aggression(),
                        preparation.safety(),
                        preparation.risk(),
                        preparation.explanation()
                    )
                );
            }
        }

        return List.copyOf(candidates.values());
    }

    private static Move castleMove(Color color) {
        return color == Color.WHITE
            ? Move.fromUci("e1g1")
            : Move.fromUci("e8g8");
    }

    private static List<MovePreparation> preparations(Color color) {
        if (color == Color.WHITE) {
            return List.of(
                prep("g1f3", 7.5, 5.0, 8.0, 2.0, "Développer le cavalier g1."),
                prep("f1e2", 7.0, 4.0, 8.0, 2.0, "Libérer f1 avec un développement sobre."),
                prep("f1d3", 7.2, 6.0, 7.0, 3.0, "Développer le fou vers une case active."),
                prep("f1c4", 7.2, 7.0, 6.0, 4.0, "Développer le fou de manière plus offensive."),
                prep("e2e3", 6.5, 4.0, 8.0, 2.0, "Libérer la diagonale du fou f1."),
                prep("e2e4", 7.0, 7.0, 6.0, 4.0, "Libérer le fou tout en prenant de l'espace."),
                prep("g2g3", 6.2, 4.0, 8.0, 3.0, "Préparer un développement du fou en g2.")
            );
        }

        return List.of(
            prep("g8f6", 7.5, 5.0, 8.0, 2.0, "Développer le cavalier g8."),
            prep("f8e7", 7.0, 4.0, 8.0, 2.0, "Libérer f8 avec un développement sobre."),
            prep("f8d6", 7.2, 6.0, 7.0, 3.0, "Développer le fou vers une case active."),
            prep("f8c5", 7.2, 7.0, 6.0, 4.0, "Développer le fou de manière plus offensive."),
            prep("e7e6", 6.5, 4.0, 8.0, 2.0, "Libérer la diagonale du fou f8."),
            prep("e7e5", 7.0, 7.0, 6.0, 4.0, "Libérer le fou tout en prenant de l'espace."),
            prep("g7g6", 6.2, 4.0, 8.0, 3.0, "Préparer un développement du fou en g7.")
        );
    }

    private static MovePreparation prep(
        String uci,
        double score,
        double aggression,
        double safety,
        double risk,
        String explanation
    ) {
        return new MovePreparation(
            Move.fromUci(uci),
            score,
            aggression,
            safety,
            risk,
            explanation
        );
    }

    private record MovePreparation(
        Move move,
        double score,
        double aggression,
        double safety,
        double risk,
        String explanation
    ) {
    }
}
