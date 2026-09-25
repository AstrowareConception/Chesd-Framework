package fr.astroware.chess.bot.opening;

import fr.astroware.chess.bot.evaluation.EvaluationScore;
import fr.astroware.chess.core.model.Move;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Une variante d'ouverture décrite par une suite ordonnée de demi-coups.
 *
 * <p>La ligne est indépendante du moteur d'échecs : elle ne décide jamais
 * qu'un coup est légal. Elle vérifie uniquement que l'historique joué
 * correspond encore à son préfixe.</p>
 */
public record OpeningLine(
    String name,
    List<Move> moves,
    EvaluationScore aggression,
    EvaluationScore safety,
    EvaluationScore risk
) {

    public OpeningLine {
        Objects.requireNonNull(name, "name must not be null");
        moves = List.copyOf(Objects.requireNonNull(moves, "moves must not be null"));
        Objects.requireNonNull(aggression, "aggression must not be null");
        Objects.requireNonNull(safety, "safety must not be null");
        Objects.requireNonNull(risk, "risk must not be null");

        if (name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }

        if (moves.isEmpty()) {
            throw new IllegalArgumentException(
                "An opening line must contain at least one move"
            );
        }
    }

    public static OpeningLine of(String name, String... uciMoves) {
        return styled(name, 5.0, 5.0, 5.0, uciMoves);
    }

    public static OpeningLine styled(
        String name,
        double aggression,
        double safety,
        double risk,
        String... uciMoves
    ) {
        List<Move> moves = Arrays.stream(uciMoves)
            .map(Move::fromUci)
            .toList();

        return new OpeningLine(
            name,
            moves,
            EvaluationScore.of(aggression),
            EvaluationScore.of(safety),
            EvaluationScore.of(risk)
        );
    }

    /**
     * Indique si les coups déjà joués correspondent au début de cette ligne.
     */
    public boolean matchesPrefix(List<Move> history) {
        Objects.requireNonNull(history, "history must not be null");

        if (history.size() > moves.size()) {
            return false;
        }

        for (int index = 0; index < history.size(); index++) {
            if (!moves.get(index).equals(history.get(index))) {
                return false;
            }
        }

        return true;
    }

    /**
     * Retourne le prochain coup théorique si la partie suit encore la ligne.
     */
    public Optional<Move> nextMove(List<Move> history) {
        if (!matchesPrefix(history) || history.size() >= moves.size()) {
            return Optional.empty();
        }

        return Optional.of(moves.get(history.size()));
    }
}
