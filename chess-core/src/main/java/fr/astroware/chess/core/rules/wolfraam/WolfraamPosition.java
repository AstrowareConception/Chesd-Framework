package fr.astroware.chess.core.rules.wolfraam;

import fr.astroware.chess.core.model.Color;
import fr.astroware.chess.core.model.Move;
import fr.astroware.chess.core.model.Piece;
import fr.astroware.chess.core.model.PieceType;
import fr.astroware.chess.core.model.PlacedPiece;
import fr.astroware.chess.core.model.PositionView;
import fr.astroware.chess.core.model.Square;
import io.github.wolfraam.chessgame.ChessGame;
import io.github.wolfraam.chessgame.board.Side;

import java.util.List;
import java.util.Optional;

/**
 * Adaptateur privé d'une position chess-game vers notre modèle public.
 */
final class WolfraamPosition implements PositionView {

    private final ChessGame game;

    WolfraamPosition(ChessGame game) {
        this.game = game;
    }

    ChessGame game() {
        return game;
    }

    @Override
    public Optional<Piece> pieceAt(Square square) {
        io.github.wolfraam.chessgame.board.Piece piece =
            game.getPiece(toExternal(square));

        return piece == null
            ? Optional.empty()
            : Optional.of(toDomain(piece));
    }

    @Override
    public List<PlacedPiece> pieces() {
        return game.getOccupiedSquares().stream()
            .map(square -> new PlacedPiece(
                toDomain(game.getPiece(square)),
                Square.from(square.name)
            ))
            .toList();
    }

    @Override
    public List<PlacedPiece> pieces(Color color) {
        return pieces().stream()
            .filter(piece -> piece.piece().color() == color)
            .toList();
    }

    @Override
    public Color sideToMove() {
        return game.getSideToMove() == Side.WHITE
            ? Color.WHITE
            : Color.BLACK;
    }

    @Override
    public Optional<Move> lastMove() {
        io.github.wolfraam.chessgame.move.Move move = game.getLastMove();

        return move == null
            ? Optional.empty()
            : Optional.of(toDomain(move));
    }

    @Override
    public String fen() {
        return game.getFen();
    }

    static io.github.wolfraam.chessgame.board.Square toExternal(
        Square square
    ) {
        return io.github.wolfraam.chessgame.board.Square.fromName(
            square.notation()
        );
    }

    static Move toDomain(io.github.wolfraam.chessgame.move.Move move) {
        Optional<PieceType> promotion = move.promotion == null
            ? Optional.empty()
            : Optional.of(PieceType.valueOf(move.promotion.name()));

        return new Move(
            Square.from(move.from.name),
            Square.from(move.to.name),
            promotion
        );
    }

    static io.github.wolfraam.chessgame.move.Move toExternal(Move move) {
        io.github.wolfraam.chessgame.board.PieceType promotion =
            move.promotion()
                .map(type ->
                    io.github.wolfraam.chessgame.board.PieceType.valueOf(
                        type.name()
                    )
                )
                .orElse(null);

        return new io.github.wolfraam.chessgame.move.Move(
            toExternal(move.from()),
            toExternal(move.to()),
            promotion
        );
    }

    private static Piece toDomain(
        io.github.wolfraam.chessgame.board.Piece piece
    ) {
        Color color = piece.side == Side.WHITE
            ? Color.WHITE
            : Color.BLACK;

        return new Piece(
            color,
            PieceType.valueOf(piece.pieceType.name())
        );
    }
}
