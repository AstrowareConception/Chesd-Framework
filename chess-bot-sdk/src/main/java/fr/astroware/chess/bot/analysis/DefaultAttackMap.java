package fr.astroware.chess.bot.analysis;

import fr.astroware.chess.core.model.BoardFile;
import fr.astroware.chess.core.model.BoardRank;
import fr.astroware.chess.core.model.Color;
import fr.astroware.chess.core.model.PieceType;
import fr.astroware.chess.core.model.PlacedPiece;
import fr.astroware.chess.core.model.PositionView;
import fr.astroware.chess.core.model.Square;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Implémentation par défaut de la carte d'attaque.
 */
final class DefaultAttackMap implements AttackMap {

    private static final int[][] KNIGHT_OFFSETS = {
        {1, 2}, {2, 1}, {2, -1}, {1, -2},
        {-1, -2}, {-2, -1}, {-2, 1}, {-1, 2}
    };

    private static final int[][] KING_OFFSETS = {
        {1, 0}, {1, 1}, {0, 1}, {-1, 1},
        {-1, 0}, {-1, -1}, {0, -1}, {1, -1}
    };

    private static final int[][] BISHOP_DIRECTIONS = {
        {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
    };

    private static final int[][] ROOK_DIRECTIONS = {
        {1, 0}, {-1, 0}, {0, 1}, {0, -1}
    };

    private final PositionView position;
    private final Map<PlacedPiece, Set<Square>> attacksByPiece;
    private final Map<Color, Map<Square, List<PlacedPiece>>> attackers;

    DefaultAttackMap(PositionView position) {
        this.position = Objects.requireNonNull(position, "position must not be null");
        this.attacksByPiece = new HashMap<>();
        this.attackers = new EnumMap<>(Color.class);
        attackers.put(Color.WHITE, new HashMap<>());
        attackers.put(Color.BLACK, new HashMap<>());

        build();
    }

    @Override
    public Set<Square> attackedBy(Color color) {
        return Set.copyOf(attackers.get(color).keySet());
    }

    @Override
    public List<PlacedPiece> attackersOf(Square square, Color color) {
        Objects.requireNonNull(square, "square must not be null");
        Objects.requireNonNull(color, "color must not be null");

        return List.copyOf(
            attackers.get(color).getOrDefault(square, List.of())
        );
    }

    @Override
    public int attackCount(Square square, Color color) {
        return attackersOf(square, color).size();
    }

    @Override
    public Set<Square> attacksFrom(PlacedPiece piece) {
        Objects.requireNonNull(piece, "piece must not be null");
        return Set.copyOf(attacksByPiece.getOrDefault(piece, Set.of()));
    }

    private void build() {
        for (PlacedPiece piece : position.pieces()) {
            Set<Square> attacks = computeAttacks(piece);
            attacksByPiece.put(piece, attacks);

            Map<Square, List<PlacedPiece>> bySquare =
                attackers.get(piece.piece().color());

            for (Square square : attacks) {
                bySquare
                    .computeIfAbsent(square, ignored -> new ArrayList<>())
                    .add(piece);
            }
        }
    }

    private Set<Square> computeAttacks(PlacedPiece placedPiece) {
        return switch (placedPiece.piece().type()) {
            case PAWN -> pawnAttacks(placedPiece);
            case KNIGHT -> jumpAttacks(placedPiece.square(), KNIGHT_OFFSETS);
            case BISHOP -> slidingAttacks(placedPiece.square(), BISHOP_DIRECTIONS);
            case ROOK -> slidingAttacks(placedPiece.square(), ROOK_DIRECTIONS);
            case QUEEN -> queenAttacks(placedPiece.square());
            case KING -> jumpAttacks(placedPiece.square(), KING_OFFSETS);
        };
    }

    private Set<Square> pawnAttacks(PlacedPiece piece) {
        int rankDirection = piece.piece().color() == Color.WHITE ? 1 : -1;
        Set<Square> result = new HashSet<>();

        offset(piece.square(), -1, rankDirection).ifPresent(result::add);
        offset(piece.square(), 1, rankDirection).ifPresent(result::add);

        return Set.copyOf(result);
    }

    private Set<Square> queenAttacks(Square origin) {
        Set<Square> result = new HashSet<>(
            slidingAttacks(origin, BISHOP_DIRECTIONS)
        );
        result.addAll(slidingAttacks(origin, ROOK_DIRECTIONS));
        return Set.copyOf(result);
    }

    private Set<Square> jumpAttacks(Square origin, int[][] offsets) {
        Set<Square> result = new HashSet<>();

        for (int[] delta : offsets) {
            offset(origin, delta[0], delta[1]).ifPresent(result::add);
        }

        return Set.copyOf(result);
    }

    private Set<Square> slidingAttacks(Square origin, int[][] directions) {
        Set<Square> result = new HashSet<>();

        for (int[] direction : directions) {
            Square current = origin;

            while (true) {
                Optional<Square> next = offset(
                    current,
                    direction[0],
                    direction[1]
                );

                if (next.isEmpty()) {
                    break;
                }

                Square square = next.orElseThrow();
                result.add(square);

                if (position.pieceAt(square).isPresent()) {
                    break;
                }

                current = square;
            }
        }

        return Set.copyOf(result);
    }

    private static Optional<Square> offset(
        Square origin,
        int fileDelta,
        int rankDelta
    ) {
        int fileIndex = origin.file().ordinal() + fileDelta;
        int rankNumber = origin.rank().number() + rankDelta;

        if (fileIndex < 0
            || fileIndex >= BoardFile.values().length
            || rankNumber < 1
            || rankNumber > 8) {
            return Optional.empty();
        }

        return Optional.of(
            new Square(
                BoardFile.values()[fileIndex],
                BoardRank.from(rankNumber)
            )
        );
    }
}
