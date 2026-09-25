package fr.astroware.chess.bot.analysis;

import fr.astroware.chess.core.model.PieceType;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Valeurs matérielles associées aux pièces.
 *
 * <p>La première version utilise les valeurs pédagogiques classiques :
 * pion 1, cavalier 3, fou 3, tour 5 et dame 9. Le roi n'est pas évalué
 * matériellement car sa perte n'est pas un échange possible : elle termine
 * la partie.</p>
 */
public final class PieceValues {

    private final Map<PieceType, Integer> values;

    private PieceValues(Map<PieceType, Integer> values) {
        this.values = Map.copyOf(values);
    }

    public static PieceValues standard() {
        EnumMap<PieceType, Integer> values = new EnumMap<>(PieceType.class);
        values.put(PieceType.PAWN, 1);
        values.put(PieceType.KNIGHT, 3);
        values.put(PieceType.BISHOP, 3);
        values.put(PieceType.ROOK, 5);
        values.put(PieceType.QUEEN, 9);
        values.put(PieceType.KING, 0);

        return new PieceValues(values);
    }

    public int valueOf(PieceType type) {
        Objects.requireNonNull(type, "type must not be null");
        return values.get(type);
    }
}
