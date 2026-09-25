package fr.astroware.chess.tournament.execution;

/**
 * Catégorie d'échec d'une exécution isolée.
 */
public enum BotExecutionFailure {
    BOT_EXCEPTION,
    TIMEOUT,
    PROCESS_FAILURE,
    PROTOCOL_ERROR
}
