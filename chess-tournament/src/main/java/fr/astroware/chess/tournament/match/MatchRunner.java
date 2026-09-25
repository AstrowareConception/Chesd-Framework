package fr.astroware.chess.tournament.match;

import fr.astroware.chess.bot.api.BotContext;
import fr.astroware.chess.bot.api.BotDecision;
import fr.astroware.chess.bot.api.BotMetadata;
import fr.astroware.chess.core.game.GameResult;
import fr.astroware.chess.core.model.Color;
import fr.astroware.chess.core.model.Move;
import fr.astroware.chess.core.model.PositionView;
import fr.astroware.chess.core.rules.ChessRulesEngine;
import fr.astroware.chess.core.rules.ChessRulesEngines;
import fr.astroware.chess.tournament.execution.BotExecutionException;
import fr.astroware.chess.tournament.execution.BotExecutionFailure;
import fr.astroware.chess.tournament.execution.BotPlayer;
import fr.astroware.chess.tournament.execution.BotPlayerFactory;
import fr.astroware.chess.tournament.execution.BotPlayers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.random.RandomGenerator;

/**
 * Exécute une partie complète entre deux joueurs de bot.
 *
 * <p>Les appels historiques avec {@link BotFactory} utilisent une exécution
 * dans la JVM courante. Une autre {@link BotPlayerFactory} peut fournir un
 * processus isolé sans modifier le moteur de partie.</p>
 */
public final class MatchRunner {

    private static final long WHITE_SEED_SALT =
        0x51A7E123L;

    private static final long BLACK_SEED_SALT =
        0xB1AC2026L;

    private final ChessRulesEngine engine;

    public MatchRunner() {
        this(ChessRulesEngines.standard());
    }

    public MatchRunner(ChessRulesEngine engine) {
        this.engine = java.util.Objects.requireNonNull(
            engine,
            "engine must not be null"
        );
    }

    public MatchResult play(
        BotFactory whiteFactory,
        BotFactory blackFactory,
        MatchConfiguration configuration
    ) {
        return play(
            BotPlayers.inProcess(whiteFactory),
            BotPlayers.inProcess(blackFactory),
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
        return play(
            BotPlayers.inProcess(whiteFactory),
            BotPlayers.inProcess(blackFactory),
            configuration,
            listener
        );
    }

    /**
     * Exécute une partie avec des stratégies d'exécution configurables.
     */
    public MatchResult play(
        BotPlayerFactory<? extends BotPlayer> whiteFactory,
        BotPlayerFactory<? extends BotPlayer> blackFactory,
        MatchConfiguration configuration
    ) {
        return play(
            whiteFactory,
            blackFactory,
            configuration,
            MatchListeners.none()
        );
    }

    /**
     * Exécute une partie avec des joueurs pouvant être locaux ou isolés.
     */
    public MatchResult play(
        BotPlayerFactory<? extends BotPlayer> whiteFactory,
        BotPlayerFactory<? extends BotPlayer> blackFactory,
        MatchConfiguration configuration,
        MatchListener listener
    ) {
        java.util.Objects.requireNonNull(
            whiteFactory,
            "whiteFactory must not be null"
        );
        java.util.Objects.requireNonNull(
            blackFactory,
            "blackFactory must not be null"
        );
        java.util.Objects.requireNonNull(
            configuration,
            "configuration must not be null"
        );
        java.util.Objects.requireNonNull(
            listener,
            "listener must not be null"
        );

        long whiteSeed =
            configuration.randomSeed() ^ WHITE_SEED_SALT;

        long blackSeed =
            configuration.randomSeed() ^ BLACK_SEED_SALT;

        try (
            BotPlayer white = whiteFactory.create(whiteSeed);
            BotPlayer black = blackFactory.create(blackSeed)
        ) {
            return playOpenedPlayers(
                white,
                black,
                configuration,
                listener,
                whiteSeed,
                blackSeed
            );
        }
    }

    private MatchResult playOpenedPlayers(
        BotPlayer white,
        BotPlayer black,
        MatchConfiguration configuration,
        MatchListener listener,
        long whiteSeed,
        long blackSeed
    ) {
        BotMetadata whiteMetadata =
            java.util.Objects.requireNonNull(
                white.metadata(),
                "white metadata must not be null"
            );

        BotMetadata blackMetadata =
            java.util.Objects.requireNonNull(
                black.metadata(),
                "black metadata must not be null"
            );

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
            new Random(whiteSeed);

        RandomGenerator blackRandom =
            new Random(blackSeed);

        for (
            int ply = 1;
            ply <= configuration.maxPlies();
            ply++
        ) {
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

            BotPlayer player =
                color == Color.WHITE ? white : black;

            BotMetadata playerMetadata =
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
                decision = player.decide(context);
            } catch (BotExecutionException exception) {
                return finishForfeit(
                    whiteMetadata,
                    blackMetadata,
                    color,
                    playerMetadata,
                    mapExecutionFailure(exception.failure()),
                    exception.remoteExceptionClass(),
                    safeMessage(exception),
                    ply,
                    playedMoves,
                    initialFen,
                    position,
                    listener
                );
            } catch (RuntimeException exception) {
                return finishForfeit(
                    whiteMetadata,
                    blackMetadata,
                    color,
                    playerMetadata,
                    MatchIncidentType.BOT_EXCEPTION,
                    exception.getClass().getName(),
                    safeMessage(exception),
                    ply,
                    playedMoves,
                    initialFen,
                    position,
                    listener
                );
            }

            long decisionNanos =
                System.nanoTime() - decisionStartedAt;

            if (decision == null
                || decision.move() == null
                || !legalMoves.contains(decision.move())) {

                String move = decision == null
                    || decision.move() == null
                    ? "<null>"
                    : decision.move().toUci();

                return finishForfeit(
                    whiteMetadata,
                    blackMetadata,
                    color,
                    playerMetadata,
                    MatchIncidentType.BOT_ILLEGAL_MOVE,
                    "",
                    "Coup illégal ou absent proposé : " + move,
                    ply,
                    playedMoves,
                    initialFen,
                    position,
                    listener
                );
            }

            String beforeFen = position.fen();
            String san =
                engine.toSan(position, decision.move());

            PositionView afterPosition =
                engine.play(position, decision.move());

            history.add(decision.move());

            PlayedMove playedMove = new PlayedMove(
                ply,
                color,
                playerMetadata,
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
        MatchIncidentType incidentType,
        String exceptionClass,
        String message,
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
            incidentType,
            offenderColor,
            offender,
            exceptionClass,
            message,
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

    private static MatchIncidentType mapExecutionFailure(
        BotExecutionFailure failure
    ) {
        return switch (failure) {
            case BOT_EXCEPTION ->
                MatchIncidentType.BOT_EXCEPTION;
            case TIMEOUT ->
                MatchIncidentType.BOT_TIMEOUT;
            case PROCESS_FAILURE ->
                MatchIncidentType.BOT_PROCESS_FAILURE;
            case PROTOCOL_ERROR ->
                MatchIncidentType.BOT_PROTOCOL_ERROR;
        };
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
