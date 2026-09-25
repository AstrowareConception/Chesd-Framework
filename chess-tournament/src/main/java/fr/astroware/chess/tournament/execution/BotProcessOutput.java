package fr.astroware.chess.tournament.execution;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Capture bornée de stdout/stderr d'une JVM enfant.
 *
 * <p>La lecture permanente empêche le buffer système de bloquer le bot même
 * s'il écrit beaucoup sur la console.</p>
 */
final class BotProcessOutput {

    private static final int MAX_CHARS = 32_768;

    private final StringBuilder buffer =
        new StringBuilder();

    private BotProcessOutput() {
    }

    static BotProcessOutput start(
        Process process
    ) {
        BotProcessOutput capture =
            new BotProcessOutput();

        Thread.ofVirtual()
            .name("chess-bot-output")
            .start(
                () -> capture.consume(
                    process.getInputStream()
                )
            );

        return capture;
    }

    synchronized String snapshot() {
        return buffer.toString();
    }

    private void consume(InputStream input) {
        try (input) {
            byte[] bytes = new byte[4096];
            int read;

            while ((read = input.read(bytes)) >= 0) {
                append(
                    new String(
                        bytes,
                        0,
                        read,
                        StandardCharsets.UTF_8
                    )
                );
            }
        } catch (IOException ignored) {
            // Le processus peut disparaître pendant la lecture.
        }
    }

    private synchronized void append(
        String text
    ) {
        buffer.append(text);

        if (buffer.length() > MAX_CHARS) {
            buffer.delete(
                0,
                buffer.length() - MAX_CHARS
            );
        }
    }
}
