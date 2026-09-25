package fr.astroware.chess.core.rules;

import fr.astroware.chess.core.model.PositionView;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests perft de non-régression du moteur de règles.
 *
 * <p>Les valeurs 20 / 400 / 8902 de la position initiale sont les comptes
 * classiques de nœuds légaux aux profondeurs 1, 2 et 3. Elles exercent à la
 * fois la génération de coups et l'application successive des positions.</p>
 */
class ChessRulesPerftTest {

    private final ChessRulesEngine engine =
        ChessRulesEngines.standard();

    @Test
    void initialPositionPerftDepthOne() {
        assertEquals(
            20L,
            perft(
                engine.initialPosition(),
                1
            )
        );
    }

    @Test
    void initialPositionPerftDepthTwo() {
        assertEquals(
            400L,
            perft(
                engine.initialPosition(),
                2
            )
        );
    }

    @Test
    void initialPositionPerftDepthThree() {
        assertEquals(
            8_902L,
            perft(
                engine.initialPosition(),
                3
            )
        );
    }

    private long perft(
        PositionView position,
        int depth
    ) {
        if (depth == 0) {
            return 1L;
        }

        long nodes = 0L;

        for (var move
            : engine.legalMoves(position)) {

            nodes += perft(
                engine.play(
                    position,
                    move
                ),
                depth - 1
            );
        }

        return nodes;
    }
}
