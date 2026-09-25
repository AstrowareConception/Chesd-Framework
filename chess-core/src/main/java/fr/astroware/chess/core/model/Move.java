package fr.astroware.chess.core.model;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * Déplacement proposé sur l'échiquier.
 *
 * <p>La légalité complète d'un coup n'est pas la responsabilité de cet objet.
 * Elle appartient au moteur de règles. {@code Move} décrit uniquement une
 * intention de déplacement.</p>
 */
public record Move(
    Square from,
    Square to,
    Optional<PieceType> promotion
) {

    public Move {
        Objects.requireNonNull(from, "from must not be null");
        Objects.requireNonNull(to, "to must not be null");
        Objects.requireNonNull(promotion, "promotion must not be null");

        promotion.ifPresent(type -> {
            if (type == PieceType.KING || type == PieceType.PAWN) {
                throw new IllegalArgumentException(
                    "A pawn cannot be promoted to " + type
                );
            }
        });
    }

    public static Move of(String from, String to) {
        return new Move(
            Square.from(from),
            Square.from(to),
            Optional.empty()
        );
    }

    public static Move promotion(String from, String to, PieceType pieceType) {
        return new Move(
            Square.from(from),
            Square.from(to),
            Optional.of(Objects.requireNonNull(pieceType))
        );
    }

    /**
     * Construit un coup depuis une notation UCI simple.
     *
     * <p>Exemples : {@code e2e4}, {@code g1f3}, {@code e7e8q}.</p>
     */
    public static Move fromUci(String notation) {
        Objects.requireNonNull(notation, "notation must not be null");

        String normalized = notation.trim().toLowerCase(Locale.ROOT);

        if (normalized.length() != 4 && normalized.length() != 5) {
            throw new IllegalArgumentException(
                "A UCI move must look like e2e4 or e7e8q: " + notation
            );
        }

        String from = normalized.substring(0, 2);
        String to = normalized.substring(2, 4);

        if (normalized.length() == 4) {
            return of(from, to);
        }

        return promotion(
            from,
            to,
            promotionType(normalized.charAt(4))
        );
    }

    /**
     * Retourne la représentation UCI du coup.
     */
    public String toUci() {
        String base = from.notation() + to.notation();

        if (promotion.isEmpty()) {
            return base;
        }

        return base + promotionSymbol(promotion.orElseThrow());
    }

    private static PieceType promotionType(char symbol) {
        return switch (symbol) {
            case 'q' -> PieceType.QUEEN;
            case 'r' -> PieceType.ROOK;
            case 'b' -> PieceType.BISHOP;
            case 'n' -> PieceType.KNIGHT;
            default -> throw new IllegalArgumentException(
                "Unknown promotion piece: " + symbol
            );
        };
    }

    private static char promotionSymbol(PieceType type) {
        return switch (type) {
            case QUEEN -> 'q';
            case ROOK -> 'r';
            case BISHOP -> 'b';
            case KNIGHT -> 'n';
            case KING, PAWN -> throw new IllegalArgumentException(
                "Invalid promotion type: " + type
            );
        };
    }
}
