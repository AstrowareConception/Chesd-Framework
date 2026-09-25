package fr.astroware.chess.tournament.match;

import fr.astroware.chess.bot.action.Actions;
import fr.astroware.chess.bot.api.BotContext;
import fr.astroware.chess.bot.api.BotMetadata;
import fr.astroware.chess.bot.api.ChessBot;
import fr.astroware.chess.bot.rule.Rule;
import fr.astroware.chess.bot.situation.Situations;
import fr.astroware.chess.bot.strategy.StrategyProfile;
import fr.astroware.chess.bots.baseline.RandomBot;
import fr.astroware.chess.core.game.GameStatus;
import fr.astroware.chess.core.model.Color;
import fr.astroware.chess.tournament.pgn.PgnExporter;
import fr.astroware.chess.tournament.roundrobin.RoundRobinConfiguration;
import fr.astroware.chess.tournament.roundrobin.RoundRobinResult;
import fr.astroware.chess.tournament.roundrobin.RoundRobinTournament;
import fr.astroware.chess.tournament.roundrobin.TournamentParticipant;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ForfeitHandlingTest {

    @Test
    void botExceptionBecomesForfeitInsteadOfCrashingMatch() {
        MatchResult result = new MatchRunner().play(
            ExplodingBot::new,
            RandomBot::new,
            new MatchConfiguration(
                20,
                42L,
                java.util.Optional.empty()
            )
        );

        assertEquals(
            MatchTermination.FORFEIT,
            result.termination()
        );

        assertEquals(
            GameStatus.BLACK_WINS,
            result.gameResult().orElseThrow().status()
        );

        MatchIncident incident =
            result.incident().orElseThrow();

        assertEquals(
            Color.WHITE,
            incident.offenderColor()
        );

        assertEquals(
            MatchIncidentType.BOT_EXCEPTION,
            incident.type()
        );

        assertTrue(
            incident.exceptionClass()
                .contains("IllegalStateException")
        );

        String pgn = new PgnExporter().export(result);

        assertTrue(
            pgn.contains("[Termination \"forfeit\"]")
        );
        assertTrue(
            pgn.contains("[ForfeitBy \"Exploding Bot\"]")
        );
    }

    @Test
    void roundRobinContinuesAfterForfeit() {
        RoundRobinResult result =
            new RoundRobinTournament().play(
                List.of(
                    new TournamentParticipant(
                        "broken",
                        ExplodingBot::new
                    ),
                    new TournamentParticipant(
                        "random-a",
                        RandomBot::new
                    ),
                    new TournamentParticipant(
                        "random-b",
                        RandomBot::new
                    )
                ),
                new RoundRobinConfiguration(
                    1,
                    4,
                    99L
                )
            );

        // Trois participants -> trois paires, malgré les forfaits.
        assertEquals(3, result.matches().size());

        var broken = result.standings().stream()
            .filter(standing ->
                standing.bot().botName()
                    .equals("Exploding Bot")
            )
            .findFirst()
            .orElseThrow();

        assertEquals(2, broken.played());
        assertEquals(2, broken.losses());
        assertEquals(2, broken.forfeits());
        assertEquals(0.0, broken.points());
    }

    private static final class ExplodingBot
        extends ChessBot {

        @Override
        public BotMetadata metadata() {
            return new BotMetadata(
                "Exploding Bot",
                "Test Suite",
                "Bot volontairement défectueux."
            );
        }

        @Override
        protected StrategyProfile strategyProfile(
            BotContext context
        ) {
            throw new IllegalStateException(
                "Simulated student bot failure"
            );
        }

        @Override
        protected List<Rule<?>> rules() {
            return List.of(
                rule(
                    "Fallback",
                    Situations.always(),
                    Actions.randomLegalMove()
                )
            );
        }
    }
}
