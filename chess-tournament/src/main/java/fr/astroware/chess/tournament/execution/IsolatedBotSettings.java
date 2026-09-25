package fr.astroware.chess.tournament.execution;

import java.time.Duration;
import java.util.Objects;

/**
 * Limites appliquées à une JVM de bot isolée.
 *
 * @param startupTimeout délai maximal de connexion du worker
 * @param decisionTimeout délai maximal pour une décision
 * @param maxHeapMegabytes plafond de heap de la JVM enfant
 */
public record IsolatedBotSettings(
    Duration startupTimeout,
    Duration decisionTimeout,
    int maxHeapMegabytes
) {

    public IsolatedBotSettings {
        Objects.requireNonNull(
            startupTimeout,
            "startupTimeout must not be null"
        );
        Objects.requireNonNull(
            decisionTimeout,
            "decisionTimeout must not be null"
        );

        if (startupTimeout.isZero()
            || startupTimeout.isNegative()) {
            throw new IllegalArgumentException(
                "startupTimeout must be positive"
            );
        }

        if (decisionTimeout.isZero()
            || decisionTimeout.isNegative()) {
            throw new IllegalArgumentException(
                "decisionTimeout must be positive"
            );
        }

        if (maxHeapMegabytes < 32) {
            throw new IllegalArgumentException(
                "maxHeapMegabytes must be >= 32"
            );
        }
    }

    public static IsolatedBotSettings standard() {
        return new IsolatedBotSettings(
            Duration.ofSeconds(5),
            Duration.ofSeconds(2),
            256
        );
    }

    public int startupTimeoutMillis() {
        return Math.toIntExact(
            startupTimeout.toMillis()
        );
    }

    public int decisionTimeoutMillis() {
        return Math.toIntExact(
            decisionTimeout.toMillis()
        );
    }
}
