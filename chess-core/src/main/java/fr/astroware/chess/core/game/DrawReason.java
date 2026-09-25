package fr.astroware.chess.core.game;

/**
 * Cause d'une partie nulle.
 */
public enum DrawReason {
    STALEMATE,
    INSUFFICIENT_MATERIAL,
    FIFTY_MOVE_RULE,
    THREEFOLD_REPETITION
}
