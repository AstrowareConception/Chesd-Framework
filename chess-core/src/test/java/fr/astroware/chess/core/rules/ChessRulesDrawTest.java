package fr.astroware.chess.core.rules;

import fr.astroware.chess.core.game.DrawReason;
import fr.astroware.chess.core.game.GameStatus;
import fr.astroware.chess.core.model.Move;
import fr.astroware.chess.core.model.PositionView;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChessRulesDrawTest {

    private final ChessRulesEngine engine =
        ChessRulesEngines.standard();

    @Test
    void detectsStalemate() {
        PositionView position = engine.fromFen(
            "7k/5Q2/6K1/8/8/8/8/8 b - - 0 1"
        );

        assertEquals(
            GameStatus.DRAW,
            engine.result(position).status()
        );
        assertEquals(
            DrawReason.STALEMATE,
            engine.result(position)
                .drawReason()
                .orElseThrow()
        );
    }

    @Test
    void detectsInsufficientMaterial() {
        PositionView position = engine.fromFen(
            "4k3/8/8/8/8/8/8/4K3 w - - 0 1"
        );

        assertEquals(
            GameStatus.DRAW,
            engine.result(position).status()
        );
        assertEquals(
            DrawReason.INSUFFICIENT_MATERIAL,
            engine.result(position)
                .drawReason()
                .orElseThrow()
        );
    }

    @Test
    void detectsFiftyMoveRuleAfterHundredHalfMoves() {
        PositionView position = engine.fromFen(
            "4k2r/8/8/8/8/8/8/R3K3 w - - 99 50"
        );

        position = engine.play(
            position,
            Move.fromUci("a1a2")
        );

        assertEquals(
            GameStatus.DRAW,
            engine.result(position).status()
        );
        assertEquals(
            DrawReason.FIFTY_MOVE_RULE,
            engine.result(position)
                .drawReason()
                .orElseThrow()
        );
    }

    @Test
    void detectsThreefoldRepetition() {
        PositionView position =
            engine.initialPosition();

        List<String> cycle = List.of(
            "g1f3",
            "g8f6",
            "f3g1",
            "f6g8",
            "g1f3",
            "g8f6",
            "f3g1",
            "f6g8"
        );

        for (String move : cycle) {
            position = engine.play(
                position,
                Move.fromUci(move)
            );
        }

        assertEquals(
            GameStatus.DRAW,
            engine.result(position).status()
        );
        assertEquals(
            DrawReason.THREEFOLD_REPETITION,
            engine.result(position)
                .drawReason()
                .orElseThrow()
        );
    }
}
