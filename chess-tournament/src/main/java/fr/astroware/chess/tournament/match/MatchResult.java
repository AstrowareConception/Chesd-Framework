package fr.astroware.chess.tournament.match;

import fr.astroware.chess.bot.api.BotMetadata;
import fr.astroware.chess.core.game.GameResult;
import fr.astroware.chess.core.game.GameStatus;
import fr.astroware.chess.core.model.Move;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Résultat complet d'un match.
 */
public record MatchResult(
    BotMetadata white,
    BotMetadata black,
    MatchTermination termination,
    Optional<GameResult> gameResult,
    List<PlayedMove> playedMoves,
    String initialFen,
    String finalFen
) {

    public MatchResult {
        Objects.requireNonNull(white, "white must not be null");
        Objects.requireNonNull(black, "black must not be null");
        Objects.requireNonNull(termination, "termination must not be null");
        Objects.requireNonNull(gameResult, "gameResult must not be null");
        playedMoves = List.copyOf(
            Objects.requireNonNull(playedMoves, "playedMoves must not be null")
        );
        Objects.requireNonNull(initialFen, "initialFen must not be null");
        Objects.requireNonNull(finalFen, "finalFen must not be null");

        if (termination == MatchTermination.NATURAL
            && gameResult.isEmpty()) {
            throw new IllegalArgumentException(
                "A naturally terminated match must have a game result"
            );
        }
    }

    public List<Move> moves() {
        return playedMoves.stream()
            .map(played -> played.decision().move())
            .toList();
    }

    /**
     * Nombre de demi-coups joués.
     */
    public int pliesPlayed() {
        return playedMoves.size();
    }

    /**
     * Nombre de numéros de coups effectivement commencés.
     *
     * <p>Exemple : 1.e4 e5 2.Nf3 représente 3 demi-coups et 2 coups complets
     * au sens d'affichage humain.</p>
     */
    public int fullMovesPlayed() {
        return (playedMoves.size() + 1) / 2;
    }

    /**
     * Résultat standard utilisé dans un PGN.
     */
    public String pgnResult() {
        if (gameResult.isEmpty()) {
            return "*";
        }

        return switch (gameResult.orElseThrow().status()) {
            case WHITE_WINS -> "1-0";
            case BLACK_WINS -> "0-1";
            case DRAW -> "1/2-1/2";
            case ONGOING -> "*";
        };
    }

    public boolean isFinishedNaturally() {
        return termination == MatchTermination.NATURAL
            && gameResult
                .map(GameResult::status)
                .filter(status -> status != GameStatus.ONGOING)
                .isPresent();
    }
}
