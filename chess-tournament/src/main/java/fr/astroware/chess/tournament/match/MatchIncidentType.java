package fr.astroware.chess.tournament.match;

/**
 * Type d'incident empêchant une partie de se terminer normalement.
 */
public enum MatchIncidentType {
    BOT_EXCEPTION,
    BOT_TIMEOUT,
    BOT_PROCESS_FAILURE,
    BOT_PROTOCOL_ERROR,
    BOT_ILLEGAL_MOVE
}
