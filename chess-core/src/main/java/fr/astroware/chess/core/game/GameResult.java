package fr.astroware.chess.core.game;

import java.util.Objects;
import java.util.Optional;

/**
 * Résultat courant ou final d'une partie.
 *
 * @param status état global
 * @param drawReason cause de la nulle, uniquement si status vaut DRAW
 */
public record GameResult(
    GameStatus status,
    Optional<DrawReason> drawReason
) {

    public GameResult {
        Objects.requireNonNull(status, "status must not be null");
        Objects.requireNonNull(drawReason, "drawReason must not be null");

        if (status != GameStatus.DRAW && drawReason.isPresent()) {
            throw new IllegalArgumentException(
                "drawReason is only valid for a draw"
            );
        }
    }

    public static GameResult ongoing() {
        return new GameResult(GameStatus.ONGOING, Optional.empty());
    }

    public static GameResult whiteWins() {
        return new GameResult(GameStatus.WHITE_WINS, Optional.empty());
    }

    public static GameResult blackWins() {
        return new GameResult(GameStatus.BLACK_WINS, Optional.empty());
    }

    public static GameResult draw(DrawReason reason) {
        return new GameResult(
            GameStatus.DRAW,
            Optional.of(Objects.requireNonNull(reason))
        );
    }

    public boolean isOver() {
        return status != GameStatus.ONGOING;
    }
}
