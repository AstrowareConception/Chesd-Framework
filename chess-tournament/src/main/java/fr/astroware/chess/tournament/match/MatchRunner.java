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
 * <p>Cette première version est volontairement synchrone. L'isolation dans des
 * JVM séparées et les timeouts durs appartiendront à la phase de robustesse du
 * tournoi.</p>
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
        ChessBot white = whiteFactory.create();
        ChessBot black = blackFactory.create();

        PositionView position = configuration.initialFen()
            .map(engine::fromFen)
            .orElseGet(engine::initialPosition);

        List<Move> history = new ArrayList<>();
        List<PlayedMove> playedMoves = new ArrayList<>();

        RandomGenerator whiteRandom =
            new Random(configuration.randomSeed() ^ 0x51A7E123L);
        RandomGenerator blackRandom =
            new Random(configuration.randomSeed() ^ 0xB1AC2026L);

        for (int ply = 1; ply <= configuration.maxPlies(); ply++) {
            GameResult before = engine.result(position);

            if (before.isOver()) {
                return naturalResult(
                    white,
                    black,
                    before,
                    playedMoves,
                    position
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

            BotDecision decision = bot.decide(context);
            position = engine.play(position, decision.move());
            history.add(decision.move());

            playedMoves.add(
                new PlayedMove(
                    ply,
                    color,
                    bot.metadata(),
                    decision
                )
            );

            GameResult after = engine.result(position);

            if (after.isOver()) {
                return naturalResult(
                    white,
                    black,
                    after,
                    playedMoves,
                    position
                );
            }
        }

        return new MatchResult(
            white.metadata(),
            black.metadata(),
            MatchTermination.MOVE_LIMIT,
            Optional.empty(),
            playedMoves,
            position.fen()
        );
    }

    private static MatchResult naturalResult(
        ChessBot white,
        ChessBot black,
        GameResult gameResult,
        List<PlayedMove> moves,
        PositionView position
    ) {
        return new MatchResult(
            white.metadata(),
            black.metadata(),
            MatchTermination.NATURAL,
            Optional.of(gameResult),
            moves,
            position.fen()
        );
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
