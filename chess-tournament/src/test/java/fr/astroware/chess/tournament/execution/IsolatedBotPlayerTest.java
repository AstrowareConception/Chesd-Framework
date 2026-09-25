package fr.astroware.chess.tournament.execution;

import fr.astroware.chess.bot.action.Actions;
import fr.astroware.chess.bot.api.BotContext;
import fr.astroware.chess.bot.api.BotMetadata;
import fr.astroware.chess.bot.api.ChessBot;
import fr.astroware.chess.bot.rule.Rule;
import fr.astroware.chess.bot.situation.Situations;
import fr.astroware.chess.bot.strategy.StrategyProfile;
import fr.astroware.chess.bot.strategy.StrategyProfiles;
import fr.astroware.chess.bots.baseline.RandomBot;
import fr.astroware.chess.core.game.GameStatus;
import fr.astroware.chess.core.model.Color;
import fr.astroware.chess.tournament.match.MatchConfiguration;
import fr.astroware.chess.tournament.match.MatchIncidentType;
import fr.astroware.chess.tournament.match.MatchResult;
import fr.astroware.chess.tournament.match.MatchRunner;
import fr.astroware.chess.tournament.match.MatchTermination;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IsolatedBotPlayerTest {

    @Test
    void isolatedRandomBotCanPlayAndReturnFullTrace() {
        IsolatedBotPlayerFactory isolated =
            new IsolatedBotPlayerFactory(
                RandomBot.class,
                testSettings(
                    Duration.ofSeconds(2)
                )
            );

        MatchResult result =
            new MatchRunner().play(
                isolated,
                BotPlayers.inProcess(
                    RandomBot::new
                ),
                new MatchConfiguration(
                    4,
                    123L,
                    java.util.Optional.empty()
                )
            );

        assertFalse(
            result.playedMoves().isEmpty()
        );

        assertFalse(
            result.playedMoves()
                .getFirst()
                .decision()
                .trace()
                .isEmpty()
        );
    }

    @Test
    void studentStdoutDoesNotCorruptProtocol() {
        IsolatedBotPlayerFactory isolated =
            new IsolatedBotPlayerFactory(
                NoisyBot.class,
                testSettings(
                    Duration.ofSeconds(2)
                )
            );

        MatchResult result =
            new MatchRunner().play(
                isolated,
                BotPlayers.inProcess(
                    RandomBot::new
                ),
                new MatchConfiguration(
                    2,
                    456L,
                    java.util.Optional.empty()
                )
            );

        assertFalse(result.isForfeit());
        assertEquals(2, result.pliesPlayed());
    }

    @Test
    void infiniteLoopBecomesHardTimeoutForfeit() {
        IsolatedBotPlayerFactory isolated =
            new IsolatedBotPlayerFactory(
                HangingBot.class,
                testSettings(
                    Duration.ofMillis(250)
                )
            );

        long started = System.nanoTime();

        MatchResult result =
            new MatchRunner().play(
                isolated,
                BotPlayers.inProcess(
                    RandomBot::new
                ),
                new MatchConfiguration(
                    10,
                    789L,
                    java.util.Optional.empty()
                )
            );

        double elapsedSeconds =
            (System.nanoTime() - started)
                / 1_000_000_000.0;

        assertEquals(
            MatchTermination.FORFEIT,
            result.termination()
        );

        assertEquals(
            GameStatus.BLACK_WINS,
            result.gameResult()
                .orElseThrow()
                .status()
        );

        assertEquals(
            MatchIncidentType.BOT_TIMEOUT,
            result.incident()
                .orElseThrow()
                .type()
        );

        assertEquals(
            Color.WHITE,
            result.incident()
                .orElseThrow()
                .offenderColor()
        );

        // Le timeout de décision vaut 250 ms. On laisse une marge généreuse
        // au démarrage d'une JVM sur le runner CI.
        assertTrue(elapsedSeconds < 5.0);
    }

    @Test
    void remoteExceptionIsReportedAsBotException() {
        IsolatedBotPlayerFactory isolated =
            new IsolatedBotPlayerFactory(
                ExplodingRemoteBot.class,
                testSettings(
                    Duration.ofSeconds(2)
                )
            );

        MatchResult result =
            new MatchRunner().play(
                isolated,
                BotPlayers.inProcess(
                    RandomBot::new
                ),
                new MatchConfiguration(
                    4,
                    321L,
                    java.util.Optional.empty()
                )
            );

        assertEquals(
            MatchIncidentType.BOT_EXCEPTION,
            result.incident()
                .orElseThrow()
                .type()
        );

        assertTrue(
            result.incident()
                .orElseThrow()
                .exceptionClass()
                .contains(
                    "IllegalStateException"
                )
        );
    }

    private static IsolatedBotSettings testSettings(
        Duration decisionTimeout
    ) {
        return new IsolatedBotSettings(
            Duration.ofSeconds(5),
            decisionTimeout,
            64
        );
    }

    public static final class NoisyBot
        extends ChessBot {

        public NoisyBot() {
        }

        @Override
        public BotMetadata metadata() {
            return new BotMetadata(
                "Noisy Bot",
                "Test Suite",
                "Écrit volontairement sur stdout."
            );
        }

        @Override
        protected List<Rule<?>> rules() {
            return List.of(
                rule(
                    "Bruit puis hasard",
                    Situations.always(),
                    (context, detections) -> {
                        System.out.println(
                            "STUDENT OUTPUT SHOULD NOT BREAK PROTOCOL"
                        );
                        return Actions
                            .<fr.astroware.chess.bot.rule.PresenceDetection>
                                randomLegalMove()
                            .evaluate(
                                context,
                                detections
                            );
                    }
                )
            );
        }
    }

    public static final class HangingBot
        extends ChessBot {

        public HangingBot() {
        }

        @Override
        public BotMetadata metadata() {
            return new BotMetadata(
                "Hanging Bot",
                "Test Suite",
                "Boucle infinie volontaire."
            );
        }

        @Override
        protected StrategyProfile strategyProfile(
            BotContext context
        ) {
            while (true) {
                Thread.onSpinWait();
            }
        }

        @Override
        protected List<Rule<?>> rules() {
            return List.of(
                rule(
                    "Jamais atteint",
                    Situations.always(),
                    Actions.randomLegalMove()
                )
            );
        }
    }

    public static final class ExplodingRemoteBot
        extends ChessBot {

        public ExplodingRemoteBot() {
        }

        @Override
        public BotMetadata metadata() {
            return new BotMetadata(
                "Remote Exploding Bot",
                "Test Suite",
                "Exception volontaire dans la JVM enfant."
            );
        }

        @Override
        protected StrategyProfile strategyProfile(
            BotContext context
        ) {
            throw new IllegalStateException(
                "Remote simulated failure"
            );
        }

        @Override
        protected List<Rule<?>> rules() {
            return List.of(
                rule(
                    "Jamais atteint",
                    Situations.always(),
                    Actions.randomLegalMove()
                )
            );
        }
    }
}
