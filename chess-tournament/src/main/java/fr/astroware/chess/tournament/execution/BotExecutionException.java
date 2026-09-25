package fr.astroware.chess.tournament.execution;

import java.util.Objects;

/**
 * Échec provenant de la couche d'exécution d'un bot.
 */
public final class BotExecutionException
    extends RuntimeException {

    private final BotExecutionFailure failure;
    private final String remoteExceptionClass;

    public BotExecutionException(
        BotExecutionFailure failure,
        String message
    ) {
        this(failure, "", message, null);
    }

    public BotExecutionException(
        BotExecutionFailure failure,
        String remoteExceptionClass,
        String message
    ) {
        this(
            failure,
            remoteExceptionClass,
            message,
            null
        );
    }

    public BotExecutionException(
        BotExecutionFailure failure,
        String remoteExceptionClass,
        String message,
        Throwable cause
    ) {
        super(message, cause);
        this.failure = Objects.requireNonNull(
            failure,
            "failure must not be null"
        );
        this.remoteExceptionClass =
            Objects.requireNonNullElse(
                remoteExceptionClass,
                ""
            );
    }

    public BotExecutionFailure failure() {
        return failure;
    }

    public String remoteExceptionClass() {
        return remoteExceptionClass;
    }
}
