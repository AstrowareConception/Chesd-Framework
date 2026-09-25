package fr.astroware.chess.bots.baseline;

import fr.astroware.chess.bot.api.BotContext;
import fr.astroware.chess.bot.api.BotDecision;
import fr.astroware.chess.core.game.GameStatus;
import fr.astroware.chess.core.model.Color;
import fr.astroware.chess.core.model.Move;
import fr.astroware.chess.core.model.PositionView;
import fr.astroware.chess.core.rules.ChessRulesEngine;
import fr.astroware.chess.core.rules.ChessRulesEngines;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;
import java.util.random.RandomGenerator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TacticalBotTest {

    @Test
    void findsMateInOne() {
        ChessRulesEngine engine = ChessRulesEngines.standard();
        PositionView position = engine.fromFen(
            "7k/8/5KQ1/8/8/8/8/8 w - - 0 1"
        );

        BotDecision decision =
            new TacticalBot().decide(context(engine, Color.WHITE, position));

        PositionView after = engine.play(position, decision.move());

        assertEquals(
            GameStatus.WHITE_WINS,
            engine.result(after).status()
        );
        assertEquals(
            "Mater immédiatement",
            selectedRule(decision)
        );
    }

    @Test
    void escapesCheckBeforeFollowingOtherPlans() {
        ChessRulesEngine engine = ChessRulesEngines.standard();
        PositionView position = engine.fromFen(
            "4r2k/8/8/8/8/8/8/4K3 w - - 0 1"
        );

        BotDecision decision =
            new TacticalBot().decide(context(engine, Color.WHITE, position));

        assertTrue(engine.legalMoves(position).contains(decision.move()));
        assertEquals("Sortir d'échec", selectedRule(decision));
    }

    @Test
    void choosesHighValueKnightFork() {
        ChessRulesEngine engine = ChessRulesEngines.standard();
        PositionView position = engine.fromFen(
            "k7/3q1r2/8/8/2N5/8/8/K7 w - - 0 1"
        );

        BotDecision decision =
            new TacticalBot().decide(context(engine, Color.WHITE, position));

        assertEquals(Move.fromUci("c4e5"), decision.move());
        assertEquals("Créer une fourchette", selectedRule(decision));
    }

    @Test
    void createsAbsolutePin() {
        ChessRulesEngine engine = ChessRulesEngines.standard();
        PositionView position = engine.fromFen(
            "4k3/4n3/8/8/8/8/8/R6K w - - 0 1"
        );

        BotDecision decision =
            new TacticalBot().decide(context(engine, Color.WHITE, position));

        assertEquals(Move.fromUci("a1e1"), decision.move());
        assertEquals("Créer un clouage", selectedRule(decision));
    }

    @Test
    void createsSkewer() {
        ChessRulesEngine engine = ChessRulesEngines.standard();
        PositionView position = engine.fromFen(
            "4r3/4q3/7k/8/8/8/8/R6K w - - 0 1"
        );

        BotDecision decision =
            new TacticalBot().decide(context(engine, Color.WHITE, position));

        assertEquals(Move.fromUci("a1e1"), decision.move());
        assertEquals("Créer une enfilade", selectedRule(decision));
    }

    @Test
    void givesForcingCheckWhenNoHigherPriorityTacticExists() {
        ChessRulesEngine engine = ChessRulesEngines.standard();
        PositionView position = engine.fromFen(
            "7k/8/8/8/8/8/4R3/4K3 w - - 0 1"
        );

        BotDecision decision =
            new TacticalBot().decide(context(engine, Color.WHITE, position));

        PositionView after = engine.play(position, decision.move());

        assertEquals("Donner échec", selectedRule(decision));
        assertTrue(engine.isKingAttacked(after));
    }

    private static String selectedRule(BotDecision decision) {
        return decision.trace().stream()
            .filter(attempt -> attempt.selectedMove().isPresent())
            .findFirst()
            .orElseThrow()
            .ruleName();
    }

    private static BotContext context(
        ChessRulesEngine engine,
        Color color,
        PositionView position
    ) {
        return new EngineContext(
            color,
            position,
            engine.legalMoves(position)
        );
    }

    private record EngineContext(
        Color myColor,
        PositionView position,
        List<Move> legalMoves
    ) implements BotContext {

        @Override
        public RandomGenerator random() {
            return new Random(42L);
        }
    }
}
