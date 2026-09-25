package fr.astroware.chess.tournament.match;

import fr.astroware.chess.bot.api.BotMetadata;
import fr.astroware.chess.core.game.GameResult;
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
}
