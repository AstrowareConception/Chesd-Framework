package fr.astroware.chess.tournament.match;

import fr.astroware.chess.bot.api.BotContext;
import fr.astroware.chess.bot.api.BotDecision;
import fr.astroware.chess.bot.api.BotMetadata;
import fr.astroware.chess.bot.api.ChessBot;
import fr.astroware.chess.core.game.GameResult;
import fr.astroware.chess.core.model.Color;
import fr.astroware.chess.core.model.Move;
import fr.astroware.chess.core.model.PositionView;
import fr.astroware.chess.core.rules.ChessRulesEngine;
import fr.astroware.chess.core.rules.ChessRulesEngines;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.random.RandomGenerator;

/**
 * Exécute une partie complète entre deux ChessBot.
 *
 * <p>Une exception non gérée provenant d'un bot provoque désormais un
 * forfait propre : l'adversaire gagne, l'incident est enregistré et le
 * tournoi peut continuer.</p>
 */
public final class MatchRunner {

    private final ChessRulesEngine engine;

    public MatchRunner() {
        this(ChessRulesEngines.standard());
    }

    public MatchRunner(ChessRulesEngine engine) {
        this.engine = engine;
    }

    public MatchResult play(
        BotFactory whiteFactory,
        BotFactory blackFactory,
        MatchConfiguration configuration
    ) {
        return play(
            whiteFactory,
            blackFactory,
            configuration,
            MatchListeners.none()
        );
    }

    public MatchResult play(
        BotFactory whiteFactory,
        BotFactory blackFactory,
        MatchConfiguration configuration,
        MatchListener listener
    ) {
        ChessBot white = whiteFactory.create();
        ChessBot black = blackFactory.create();

        BotMetadata whiteMetadata = white.metadata();
        BotMetadata blackMetadata = black.metadata();

        PositionView position = configuration.initialFen()
            .map(engine::fromFen)
            .orElseGet(engine::initialPosition);

        String initialFen = position.fen();

        listener.onMatchStarted(
            whiteMetadata,
            blackMetadata,
            position
        );

        List<Move> history = new ArrayList<>();
        List<PlayedMove> playedMoves = new ArrayList<>();

        RandomGenerator whiteRandom =
            new Random(configuration.randomSeed() ^ 0x51A7E123L);
        RandomGenerator blackRandom =
            new Random(configuration.randomSeed() ^ 0xB1AC2026L);

        for (int ply = 1; ply <= configuration.maxPlies(); ply++) {
            GameResult before = engine.result(position);

            if (before.isOver()) {
                return finishNatural(
                    whiteMetadata,
                    blackMetadata,
                    before,
                    playedMoves,
                    initialFen,
                    position,
                    listener
                );
            }

            Color color = position.sideToMove();
            ChessBot bot = color == Color.WHITE ? white : black;
            BotMetadata botMetadata =
                color == Color.WHITE
                    ? whiteMetadata
                    : blackMetadata;

            RandomGenerator random =
                color == Color.WHITE
                    ? whiteRandom
                    : blackRandom;

            List<Move> legalMoves =
                engine.legalMoves(position);

            BotContext context = new MatchContext(
                color,
                position,
                legalMoves,
                List.copyOf(history),
                random
            );

            long decisionStartedAt = System.nanoTime();
            BotDecision decision;

            try {
                decision = bot.decide(context);
            } catch (RuntimeException exception) {
                return finishForfeit(
                    whiteMetadata,
                    blackMetadata,
                    color,
                    botMetadata,
                    exception,
                    ply,
                    playedMoves,
                    initialFen,
                    position,
                    listener
                );
            }

            long decisionNanos =
                System.nanoTime() - decisionStartedAt;

            String beforeFen = position.fen();
            String san =
                engine.toSan(position, decision.move());

            PositionView afterPosition =
                engine.play(position, decision.move());

            history.add(decision.move());

            PlayedMove playedMove = new PlayedMove(
                ply,
                color,
                botMetadata,
                decision,
                decisionNanos,
                san,
                beforeFen,
                afterPosition.fen()
            );

            playedMoves.add(playedMove);
            listener.onMovePlayed(playedMove);

            position = afterPosition;

            GameResult after = engine.result(position);

            if (after.isOver()) {
                return finishNatural(
                    whiteMetadata,
                    blackMetadata,
                    after,
                    playedMoves,
                    initialFen,
                    position,
                    listener
                );
            }
        }

        MatchResult result = new MatchResult(
            whiteMetadata,
            blackMetadata,
            MatchTermination.MOVE_LIMIT,
            Optional.empty(),
            Optional.empty(),
            playedMoves,
            initialFen,
            position.fen()
        );

        listener.onMatchEnded(result);
        return result;
    }

    private static MatchResult finishNatural(
        BotMetadata white,
        BotMetadata black,
        GameResult gameResult,
        List<PlayedMove> moves,
        String initialFen,
        PositionView position,
        MatchListener listener
    ) {
        MatchResult result = new MatchResult(
            white,
            black,
            MatchTermination.NATURAL,
            Optional.of(gameResult),
            Optional.empty(),
            moves,
            initialFen,
            position.fen()
        );

        listener.onMatchEnded(result);
        return result;
    }

    private static MatchResult finishForfeit(
        BotMetadata white,
        BotMetadata black,
        Color offenderColor,
        BotMetadata offender,
        RuntimeException exception,
        int ply,
        List<PlayedMove> moves,
        String initialFen,
        PositionView position,
        MatchListener listener
    ) {
        GameResult gameResult =
            offenderColor == Color.WHITE
                ? GameResult.blackWins()
                : GameResult.whiteWins();

        MatchIncident incident = new MatchIncident(
            MatchIncidentType.BOT_EXCEPTION,
            offenderColor,
            offender,
            exception.getClass().getName(),
            safeMessage(exception),
            ply
        );

        MatchResult result = new MatchResult(
            white,
            black,
            MatchTermination.FORFEIT,
            Optional.of(gameResult),
            Optional.of(incident),
            moves,
            initialFen,
            position.fen()
        );

        listener.onMatchEnded(result);
        return result;
    }

    private static String safeMessage(
        RuntimeException exception
    ) {
        String message = exception.getMessage();

        if (message == null || message.isBlank()) {
            return "";
        }

        String normalized =
            message.replaceAll("\\s+", " ").trim();

        int maxLength = 500;

        return normalized.length() <= maxLength
            ? normalized
            : normalized.substring(0, maxLength) + "…";
    }

    private record MatchContext(
        Color myColor,
        PositionView position,
        List<Move> legalMoves,
        List<Move> moveHistory,
        RandomGenerator random
    ) implements BotContext {

        private MatchContext {
            legalMoves = List.copyOf(legalMoves);
            moveHistory = List.copyOf(moveHistory);
        }
    }
}
