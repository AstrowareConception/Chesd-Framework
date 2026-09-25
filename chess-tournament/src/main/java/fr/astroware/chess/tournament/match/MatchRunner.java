package fr.astroware.chess.tournament.match;

import fr.astroware.chess.bot.api.BotContext;
import fr.astroware.chess.bot.api.BotDecision;
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
 * <p>La partie émet des événements via {@link MatchListener}. La console,
 * l'interface graphique ou d'autres observateurs peuvent donc suivre le même
 * moteur sans dupliquer la logique de jeu.</p>
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

        PositionView position = configuration.initialFen()
            .map(engine::fromFen)
            .orElseGet(engine::initialPosition);

        String initialFen = position.fen();

        listener.onMatchStarted(
            white.metadata(),
            black.metadata(),
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
                    white,
                    black,
                    before,
                    playedMoves,
                    initialFen,
                    position,
                    listener
                );
            }

            Color color = position.sideToMove();
            ChessBot bot = color == Color.WHITE ? white : black;
            RandomGenerator random =
                color == Color.WHITE ? whiteRandom : blackRandom;

            List<Move> legalMoves = engine.legalMoves(position);

            BotContext context = new MatchContext(
                color,
                position,
                legalMoves,
                List.copyOf(history),
                random
            );

            long decisionStartedAt = System.nanoTime();
            BotDecision decision = bot.decide(context);
            long decisionNanos =
                System.nanoTime() - decisionStartedAt;

            String beforeFen = position.fen();
            String san = engine.toSan(position, decision.move());

            PositionView afterPosition =
                engine.play(position, decision.move());

            history.add(decision.move());

            PlayedMove playedMove = new PlayedMove(
                ply,
                color,
                bot.metadata(),
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
                    white,
                    black,
                    after,
                    playedMoves,
                    initialFen,
                    position,
                    listener
                );
            }
        }

        MatchResult result = new MatchResult(
            white.metadata(),
            black.metadata(),
            MatchTermination.MOVE_LIMIT,
            Optional.empty(),
            playedMoves,
            initialFen,
            position.fen()
        );

        listener.onMatchEnded(result);
        return result;
    }

    private static MatchResult finishNatural(
        ChessBot white,
        ChessBot black,
        GameResult gameResult,
        List<PlayedMove> moves,
        String initialFen,
        PositionView position,
        MatchListener listener
    ) {
        MatchResult result = new MatchResult(
            white.metadata(),
            black.metadata(),
            MatchTermination.NATURAL,
            Optional.of(gameResult),
            moves,
            initialFen,
            position.fen()
        );

        listener.onMatchEnded(result);
        return result;
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
