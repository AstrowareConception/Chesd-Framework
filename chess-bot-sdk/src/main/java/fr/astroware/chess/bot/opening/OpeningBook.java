package fr.astroware.chess.bot.opening;

import fr.astroware.chess.bot.api.BotContext;
import fr.astroware.chess.bot.evaluation.EvaluatedMove;
import fr.astroware.chess.bot.rule.PresenceDetection;
import fr.astroware.chess.bot.rule.Rule;
import fr.astroware.chess.bot.situation.Situations;
import fr.astroware.chess.core.model.Color;
import fr.astroware.chess.core.model.Move;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Répertoire d'ouverture destiné à une couleur.
 *
 * <p>Plusieurs variantes peuvent coexister. Si plusieurs lignes correspondent
 * encore à l'historique, plusieurs coups sont proposés et le profil
 * stratégique du bot les départage.</p>
 */
public final class OpeningBook {

    private final String name;
    private final Color color;
    private final List<OpeningLine> lines;

    public OpeningBook(
        String name,
        Color color,
        List<OpeningLine> lines
    ) {
        this.name = requireText(name, "name");
        this.color = Objects.requireNonNull(color, "color must not be null");
        this.lines = List.copyOf(
            Objects.requireNonNull(lines, "lines must not be null")
        );

        if (this.lines.isEmpty()) {
            throw new IllegalArgumentException(
                "An opening book must contain at least one line"
            );
        }
    }

    public String name() {
        return name;
    }

    public Color color() {
        return color;
    }

    public List<OpeningLine> lines() {
        return lines;
    }

    /**
     * Retourne les prochains coups théoriques encore compatibles avec la
     * partie courante.
     *
     * <p>Un coup présent dans le livre mais devenu illégal est simplement
     * ignoré. Si aucun candidat ne subsiste, la règle d'ouverture ne produit
     * rien et ChessBot continue avec la règle suivante.</p>
     */
    public List<EvaluatedMove> candidates(BotContext context) {
        Objects.requireNonNull(context, "context must not be null");

        if (context.myColor() != color || context.position().sideToMove() != color) {
            return List.of();
        }

        Map<Move, EvaluatedMove> candidates = new LinkedHashMap<>();

        for (OpeningLine line : lines) {
            line.nextMove(context.moveHistory()).ifPresent(move -> {
                if (context.legalMoves().contains(move)) {
                    candidates.putIfAbsent(
                        move,
                        EvaluatedMove.strategic(
                            move,
                            9.0,
                            line.aggression().value(),
                            line.safety().value(),
                            line.risk().value(),
                            "Ouverture « "
                                + name
                                + " », variante « "
                                + line.name()
                                + " »"
                        )
                    );
                }
            });
        }

        return new ArrayList<>(candidates.values());
    }

    /**
     * Transforme le livre en règle directement insérable dans ChessBot.
     */
    public Rule<PresenceDetection> asRule() {
        return Rule.of(
            "Ouverture : " + name,
            Situations.always(),
            (context, detections) -> candidates(context)
        );
    }

    private static String requireText(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " must not be null");
        String normalized = value.trim();

        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }

        return normalized;
    }
}
