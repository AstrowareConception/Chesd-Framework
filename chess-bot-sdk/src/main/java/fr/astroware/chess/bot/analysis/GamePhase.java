package fr.astroware.chess.bot.analysis;

/**
 * Grande phase stratégique d'une partie.
 *
 * <p>La classification fournie par le framework est heuristique et peut être
 * remplacée plus tard par une politique personnalisée.</p>
 */
public enum GamePhase {
    OPENING,
    MIDDLEGAME,
    ENDGAME
}
