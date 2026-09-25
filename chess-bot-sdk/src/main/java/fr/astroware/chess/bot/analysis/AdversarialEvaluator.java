package fr.astroware.chess.bot.analysis;

import fr.astroware.chess.bot.api.BotContext;
import fr.astroware.chess.core.game.GameStatus;
import fr.astroware.chess.core.model.Color;
import fr.astroware.chess.core.model.Move;

import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;

/**
 * Recherche pédagogique à deux demi-coups :
 *
 * <pre>
 * mon coup -> meilleure réponse adverse -> évaluation
 * </pre>
 *
 * <p>Il ne s'agit pas encore d'un Minimax générique. Cette classe matérialise
 * explicitement le premier niveau de raisonnement adversarial.</p>
 */
final class AdversarialEvaluator {

    private AdversarialEvaluator() {
    }

    static AdversarialEvaluation evaluate(
        BotContext context,
        Move move,
        Color perspective
    ) {
        Objects.requireNonNull(context, "context must not be null");
        Objects.requireNonNull(move, "move must not be null");
        Objects.requireNonNull(
            perspective,
            "perspective must not be null"
        );

        PositionProjection first =
            context.analysis().after(move);

        PositionEvaluation immediate =
            first.analysis().positionEvaluation(perspective);

        if (first.result().isOver()) {
            double score = terminalScore(
                first.result().status(),
                perspective
            );

            return new AdversarialEvaluation(
                move,
                immediate,
                Optional.empty(),
                Optional.empty(),
                score,
                0,
                "Position terminale après "
                    + move.toUci()
                    + " : score "
                    + score
                    + "/10"
            );
        }

        if (first.legalMoves().isEmpty()) {
            return new AdversarialEvaluation(
                move,
                immediate,
                Optional.empty(),
                Optional.empty(),
                immediate.total(),
                0,
                "Aucune réponse adverse disponible"
            );
        }

        ReplyEvaluation worst = first.legalMoves().stream()
            .map(reply -> evaluateReply(
                first,
                reply,
                perspective
            ))
            .min(
                Comparator.comparingDouble(
                    ReplyEvaluation::score
                )
            )
            .orElseThrow();

        String explanation =
            "score immédiat="
                + format(immediate.total())
                + "/10, meilleure réponse adverse="
                + worst.reply().toUci()
                + ", score robuste="
                + format(worst.score())
                + "/10, réponses examinées="
                + first.legalMoves().size();

        return new AdversarialEvaluation(
            move,
            immediate,
            Optional.of(worst.reply()),
            worst.evaluation(),
            worst.score(),
            first.legalMoves().size(),
            explanation
        );
    }

    private static ReplyEvaluation evaluateReply(
        PositionProjection first,
        Move reply,
        Color perspective
    ) {
        PositionProjection second =
            first.analysis().after(reply);

        if (second.result().isOver()) {
            return new ReplyEvaluation(
                reply,
                terminalScore(
                    second.result().status(),
                    perspective
                ),
                Optional.empty()
            );
        }

        PositionEvaluation evaluation =
            second.analysis().positionEvaluation(perspective);

        return new ReplyEvaluation(
            reply,
            evaluation.total(),
            Optional.of(evaluation)
        );
    }

    private static double terminalScore(
        GameStatus status,
        Color perspective
    ) {
        return switch (status) {
            case WHITE_WINS ->
                perspective == Color.WHITE ? 10.0 : 0.0;
            case BLACK_WINS ->
                perspective == Color.BLACK ? 10.0 : 0.0;
            case DRAW -> 5.0;
            case ONGOING -> 5.0;
        };
    }

    private static String format(double value) {
        return String.format(
            java.util.Locale.ROOT,
            "%.2f",
            value
        );
    }

    private record ReplyEvaluation(
        Move reply,
        double score,
        Optional<PositionEvaluation> evaluation
    ) {
    }
}
