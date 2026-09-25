package fr.astroware.chess.bot.analysis;

import fr.astroware.chess.bot.api.BotContext;
import fr.astroware.chess.core.game.GameResult;
import fr.astroware.chess.core.model.Color;
import fr.astroware.chess.core.model.Move;
import fr.astroware.chess.core.model.Piece;
import fr.astroware.chess.core.model.PlacedPiece;
import fr.astroware.chess.core.model.PositionView;
import fr.astroware.chess.core.model.Square;
import fr.astroware.chess.core.rules.ChessRulesEngine;
import fr.astroware.chess.core.rules.ChessRulesEngines;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.random.RandomGenerator;

/**
 * Analyse positionnelle de base.
 */
final class DefaultAnalysis implements Analysis {

    private final BotContext context;
    private final AttackMap attackMap;
    private final PieceValues pieceValues;
    private final Map<Move, PositionProjection> projectionCache =
        new HashMap<>();

    DefaultAnalysis(BotContext context) {
        this.context = Objects.requireNonNull(context, "context must not be null");
        this.attackMap = new DefaultAttackMap(context.position());
        this.pieceValues = PieceValues.standard();
    }

    @Override
    public AttackMap attackMap() {
        return attackMap;
    }

    @Override
    public List<PlacedPiece> attackersOf(Square square, Color color) {
        return attackMap.attackersOf(square, color);
    }

    @Override
    public List<PlacedPiece> defendersOf(PlacedPiece piece) {
        Objects.requireNonNull(piece, "piece must not be null");
        return attackMap.attackersOf(
            piece.square(),
            piece.piece().color()
        );
    }

    @Override
    public boolean isAttacked(PlacedPiece piece) {
        Objects.requireNonNull(piece, "piece must not be null");

        return attackMap.isAttacked(
            piece.square(),
            piece.piece().color().opposite()
        );
    }

    @Override
    public boolean isDefended(PlacedPiece piece) {
        return !defendersOf(piece).isEmpty();
    }

    @Override
    public boolean isHanging(PlacedPiece piece) {
        return isAttacked(piece) && !isDefended(piece);
    }

    @Override
    public List<PlacedPiece> hangingPieces(Color color) {
        return context.position().pieces(color).stream()
            .filter(this::isHanging)
            .toList();
    }

    @Override
    public int material(Color color) {
        return context.position().pieces(color).stream()
            .map(PlacedPiece::piece)
            .map(Piece::type)
            .mapToInt(pieceValues::valueOf)
            .sum();
    }

    @Override
    public MaterialBalance materialBalance() {
        return new MaterialBalance(
            material(Color.WHITE),
            material(Color.BLACK)
        );
    }

    @Override
    public List<Move> captures() {
        Color opponent = context.myColor().opposite();

        return context.legalMoves().stream()
            .filter(move -> context.position()
                .pieceAt(move.to())
                .filter(piece -> piece.color() == opponent)
                .isPresent())
            .toList();
    }

    @Override
    public PieceValues pieceValues() {
        return pieceValues;
    }

    @Override
    public boolean isKingAttacked() {
        String fen = context.position().fen();

        if (fen == null || fen.isBlank()) {
            return false;
        }

        ChessRulesEngine engine = ChessRulesEngines.standard();
        return engine.isKingAttacked(engine.fromFen(fen));
    }

    @Override
    public List<PinPattern> pinsBy(Color color) {
        return LineTacticAnalyzer.pinsBy(context.position(), color);
    }

    @Override
    public List<SkewerPattern> skewersBy(Color color) {
        return LineTacticAnalyzer.skewersBy(
            context.position(),
            color,
            pieceValues
        );
    }

    @Override
    public List<OverloadedDefenderPattern> overloadedDefenders(Color color) {
        Map<PlacedPiece, List<PlacedPiece>> protectedByDefender =
            new HashMap<>();

        for (PlacedPiece target : context.position().pieces(color)) {
            if (target.piece().type()
                == fr.astroware.chess.core.model.PieceType.KING) {
                continue;
            }

            if (!attackMap.isAttacked(
                target.square(),
                color.opposite()
            )) {
                continue;
            }

            List<PlacedPiece> defenders = defendersOf(target);

            if (defenders.size() != 1) {
                continue;
            }

            PlacedPiece defender = defenders.getFirst();

            protectedByDefender
                .computeIfAbsent(
                    defender,
                    ignored -> new ArrayList<>()
                )
                .add(target);
        }

        return protectedByDefender.entrySet().stream()
            .filter(entry -> entry.getValue().size() >= 2)
            .map(entry -> new OverloadedDefenderPattern(
                entry.getKey(),
                entry.getValue()
            ))
            .toList();
    }

    @Override
    public PositionProjection after(Move move) {
        Objects.requireNonNull(move, "move must not be null");
        return projectionCache.computeIfAbsent(
            move,
            this::createProjection
        );
    }

    private PositionProjection createProjection(Move move) {
        String fen = context.position().fen();

        if (fen == null || fen.isBlank()) {
            throw new IllegalStateException(
                "Position projection requires a complete FEN representation"
            );
        }

        ChessRulesEngine engine = ChessRulesEngines.standard();
        PositionView source = engine.fromFen(fen);
        PositionView projected = engine.play(source, move);
        List<Move> legalMoves = engine.legalMoves(projected);
        GameResult result = engine.result(projected);

        List<Move> history = new ArrayList<>(context.moveHistory());
        history.add(move);

        BotContext projectedContext = new ProjectedContext(
            projected.sideToMove(),
            projected,
            legalMoves,
            List.copyOf(history),
            context.random()
        );

        Analysis projectedAnalysis = Analysis.of(projectedContext);

        return new PositionProjection(
            projected,
            projectedAnalysis,
            legalMoves,
            result
        );
    }

    private record ProjectedContext(
        Color myColor,
        PositionView position,
        List<Move> legalMoves,
        List<Move> moveHistory,
        RandomGenerator random
    ) implements BotContext {

        private ProjectedContext {
            legalMoves = List.copyOf(legalMoves);
            moveHistory = List.copyOf(moveHistory);
        }
    }
}
