package fr.astroware.chess.tournament.match;

import fr.astroware.chess.bot.api.BotMetadata;
import fr.astroware.chess.core.game.GameResult;
import fr.astroware.chess.core.game.GameStatus;
import fr.astroware.chess.core.model.Color;
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
    Optional<MatchIncident> incident,
    List<PlayedMove> playedMoves,
    String initialFen,
    String finalFen
) {

    public MatchResult {
        Objects.requireNonNull(white, "white must not be null");
        Objects.requireNonNull(black, "black must not be null");
        Objects.requireNonNull(
            termination,
            "termination must not be null"
        );
        Objects.requireNonNull(
            gameResult,
            "gameResult must not be null"
        );
        Objects.requireNonNull(
            incident,
            "incident must not be null"
        );

        playedMoves = List.copyOf(
            Objects.requireNonNull(
                playedMoves,
                "playedMoves must not be null"
            )
        );

        Objects.requireNonNull(
            initialFen,
            "initialFen must not be null"
        );
        Objects.requireNonNull(
            finalFen,
            "finalFen must not be null"
        );

        if ((termination == MatchTermination.NATURAL
            || termination == MatchTermination.FORFEIT)
            && gameResult.isEmpty()) {

            throw new IllegalArgumentException(
                "A natural or forfeited match must have a game result"
            );
        }

        if (termination == MatchTermination.FORFEIT
            && incident.isEmpty()) {

            throw new IllegalArgumentException(
                "A forfeited match must expose its incident"
            );
        }

        if (termination != MatchTermination.FORFEIT
            && incident.isPresent()) {

            throw new IllegalArgumentException(
                "Only a forfeited match may expose an incident"
            );
        }
    }

    public List<Move> moves() {
        return playedMoves.stream()
            .map(played -> played.decision().move())
            .toList();
    }

    public int pliesPlayed() {
        return playedMoves.size();
    }

    public int fullMovesPlayed() {
        return (playedMoves.size() + 1) / 2;
    }

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

    public double totalDecisionMillis(Color color) {
        return playedMoves.stream()
            .filter(move -> move.color() == color)
            .mapToDouble(PlayedMove::decisionMillis)
            .sum();
    }

    public double averageDecisionMillis(Color color) {
        return playedMoves.stream()
            .filter(move -> move.color() == color)
            .mapToDouble(PlayedMove::decisionMillis)
            .average()
            .orElse(0.0);
    }

    public double maxDecisionMillis(Color color) {
        return playedMoves.stream()
            .filter(move -> move.color() == color)
            .mapToDouble(PlayedMove::decisionMillis)
            .max()
            .orElse(0.0);
    }

    public boolean isFinishedNaturally() {
        return termination == MatchTermination.NATURAL
            && gameResult
                .map(GameResult::status)
                .filter(status -> status != GameStatus.ONGOING)
                .isPresent();
    }

    public boolean isForfeit() {
        return termination == MatchTermination.FORFEIT;
    }
}
