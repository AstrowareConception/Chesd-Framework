package fr.astroware.chess.core.rules;

import fr.astroware.chess.core.rules.wolfraam.WolfraamChessRulesEngine;

/**
 * Fabrique des moteurs de règles supportés par le framework.
 */
public final class ChessRulesEngines {

    private ChessRulesEngines() {
    }

    /**
     * Moteur par défaut du framework.
     */
    public static ChessRulesEngine standard() {
        return new WolfraamChessRulesEngine();
    }
}
