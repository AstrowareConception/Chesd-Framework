package fr.astroware.chess.core.rules.wolfraam;

import fr.astroware.chess.core.game.DrawReason;
import fr.astroware.chess.core.game.GameResult;
import fr.astroware.chess.core.model.Move;
import fr.astroware.chess.core.model.PositionView;
import fr.astroware.chess.core.rules.ChessRulesEngine;
import fr.astroware.chess.core.rules.IllegalChessMoveException;
import io.github.wolfraam.chessgame.ChessGame;
import io.github.wolfraam.chessgame.result.ChessGameResultType;
import io.github.wolfraam.chessgame.result.DrawType;

import java.util.List;
import java.util.Objects;

/**
 * Adapter vers la bibliothèque io.github.wolfraam:chessgame.
 *
 * <p>Aucun type de cette bibliothèque ne doit sortir de cette classe et de
 * son package d'implémentation.</p>
 */
public final class WolfraamChessRulesEngine implements ChessRulesEngine {

    @Override
    public PositionView initialPosition() {
        return new WolfraamPosition(new ChessGame());
    }

    @Override
    public PositionView fromFen(String fen) {
        Objects.requireNonNull(fen, "fen must not be null");
        return new WolfraamPosition(new ChessGame(fen));
    }

    @Override
    public List<Move> legalMoves(PositionView position) {
        ChessGame game = unwrap(position);

        return game.getLegalMoves().stream()
            .map(WolfraamPosition::toDomain)
            .toList();
    }

    @Override
    public PositionView play(PositionView position, Move move) {
        Objects.requireNonNull(move, "move must not be null");

        ChessGame game = unwrap(position).clone();
        io.github.wolfraam.chessgame.move.Move externalMove =
            WolfraamPosition.toExternal(move);

        if (!game.isLegalMove(externalMove)) {
            throw new IllegalChessMoveException(move);
        }

        game.playMove(externalMove);

        return new WolfraamPosition(game);
    }

    @Override
    public GameResult result(PositionView position) {
        ChessGame game = unwrap(position);
        ChessGameResultType result = game.getGameResultType();

        if (result == null) {
            return GameResult.ongoing();
        }

        return switch (result) {
            case WHITE_WINS -> GameResult.whiteWins();
            case BLACK_WINS -> GameResult.blackWins();
            case DRAW -> GameResult.draw(
                mapDrawReason(game.getGameResult().drawType)
            );
        };
    }

    @Override
    public boolean isKingAttacked(PositionView position) {
        return unwrap(position).isKingAttacked();
    }

    private static ChessGame unwrap(PositionView position) {
        Objects.requireNonNull(position, "position must not be null");

        if (!(position instanceof WolfraamPosition wolfraamPosition)) {
            throw new IllegalArgumentException(
                "This engine can only operate on positions it created"
            );
        }

        return wolfraamPosition.game();
    }

    private static DrawReason mapDrawReason(DrawType drawType) {
        return switch (drawType) {
            case STALE_MATE -> DrawReason.STALEMATE;
            case INSUFFICIENT_MATERIAL ->
                DrawReason.INSUFFICIENT_MATERIAL;
            case FIFTY_MOVE_RULE -> DrawReason.FIFTY_MOVE_RULE;
            case THREEFOLD_REPETITION ->
                DrawReason.THREEFOLD_REPETITION;
        };
    }
}
