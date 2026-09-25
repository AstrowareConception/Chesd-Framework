package fr.astroware.chess.tournament.execution;

import fr.astroware.chess.bot.api.BotContext;
import fr.astroware.chess.bot.api.BotDecision;
import fr.astroware.chess.bot.api.BotMetadata;
import fr.astroware.chess.bot.api.ChessBot;
import fr.astroware.chess.core.rules.ChessRulesEngines;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Proxy d'un bot exécuté dans une JVM enfant persistante.
 */
public final class IsolatedBotPlayer
    implements BotPlayer {

    private final IsolatedBotSettings settings;
    private final Process process;
    private final BotProcessOutput processOutput;
    private final Socket socket;
    private final DataInputStream in;
    private final DataOutputStream out;
    private final BotMetadata metadata;

    private long requestSequence;
    private boolean closed;

    IsolatedBotPlayer(
        Class<? extends ChessBot> botClass,
        long randomSeed,
        IsolatedBotSettings settings
    ) {
        this.settings = Objects.requireNonNull(
            settings,
            "settings must not be null"
        );

        Objects.requireNonNull(
            botClass,
            "botClass must not be null"
        );

        Process startedProcess = null;
        BotProcessOutput startedOutput = null;
        Socket acceptedSocket = null;
        DataInputStream input = null;
        DataOutputStream output = null;

        try (
            ServerSocket server =
                new ServerSocket(
                    0,
                    1,
                    InetAddress.getLoopbackAddress()
                )
        ) {
            server.setSoTimeout(
                settings.startupTimeoutMillis()
            );

            String token =
                UUID.randomUUID().toString();

            ProcessBuilder builder =
                createProcessBuilder(
                    botClass,
                    randomSeed,
                    settings,
                    server.getLocalPort(),
                    token
                );

            builder.redirectErrorStream(true);

            startedProcess = builder.start();

            startedOutput =
                BotProcessOutput.start(
                    startedProcess
                );

            try {
                acceptedSocket = server.accept();
            } catch (SocketTimeoutException exception) {
                destroy(startedProcess);

                throw new BotExecutionException(
                    BotExecutionFailure.TIMEOUT,
                    "",
                    "Bot worker did not connect within "
                        + settings.startupTimeoutMillis()
                        + " ms",
                    exception
                );
            }

            acceptedSocket.setTcpNoDelay(true);
            acceptedSocket.setSoTimeout(
                settings.startupTimeoutMillis()
            );

            input = new DataInputStream(
                acceptedSocket.getInputStream()
            );

            output = new DataOutputStream(
                acceptedSocket.getOutputStream()
            );

            validateHandshakePrefix(
                input,
                token
            );

            int status =
                input.readUnsignedByte();

            if (status
                == BotWireProtocol.HANDSHAKE_ERROR) {

                String remoteClass =
                    BotWireProtocol.readString(input);

                String remoteMessage =
                    BotWireProtocol.readString(input);

                destroy(startedProcess);

                throw new BotExecutionException(
                    BotExecutionFailure.BOT_EXCEPTION,
                    remoteClass,
                    "Bot worker initialization failed: "
                        + remoteMessage
                );
            }

            if (status
                != BotWireProtocol.HANDSHAKE_OK) {

                destroy(startedProcess);

                throw new BotExecutionException(
                    BotExecutionFailure.PROTOCOL_ERROR,
                    "Unknown handshake status: "
                        + status
                );
            }

            this.metadata =
                BotWireProtocol.readMetadata(input);

            acceptedSocket.setSoTimeout(0);
        } catch (BotExecutionException exception) {
            closeQuietly(acceptedSocket);
            destroy(startedProcess);
            throw exception;
        } catch (IOException exception) {
            closeQuietly(acceptedSocket);
            destroy(startedProcess);

            String outputSnapshot =
                startedOutput == null
                    ? ""
                    : startedOutput.snapshot();

            throw new BotExecutionException(
                BotExecutionFailure.PROCESS_FAILURE,
                "",
                "Cannot start isolated bot worker"
                    + appendOutput(outputSnapshot),
                exception
            );
        }

        this.process = startedProcess;
        this.processOutput = startedOutput;
        this.socket = acceptedSocket;
        this.in = input;
        this.out = output;
    }

    @Override
    public BotMetadata metadata() {
        return metadata;
    }

    @Override
    public synchronized BotDecision decide(
        BotContext context
    ) {
        Objects.requireNonNull(
            context,
            "context must not be null"
        );

        ensureOpen();

        if (!process.isAlive()) {
            throw processFailure(
                "Bot worker exited before decision"
            );
        }

        String fen = context.position().fen();

        if (fen == null || fen.isBlank()) {
            throw new BotExecutionException(
                BotExecutionFailure.PROTOCOL_ERROR,
                "Isolated execution requires a complete FEN"
            );
        }

        long requestId = ++requestSequence;

        try {
            socket.setSoTimeout(
                settings.decisionTimeoutMillis()
            );

            BotWireProtocol.writeDecisionRequest(
                out,
                requestId,
                context.myColor(),
                fen,
                context.legalMoves(),
                context.moveHistory()
            );

            BotWireProtocol.DecisionResponse response =
                BotWireProtocol.readDecisionResponse(
                    in
                );

            if (response.requestId()
                != requestId) {
                terminate();

                throw new BotExecutionException(
                    BotExecutionFailure.PROTOCOL_ERROR,
                    "Unexpected response id "
                        + response.requestId()
                        + ", expected "
                        + requestId
                );
            }

            if (!response.success()) {
                throw new BotExecutionException(
                    BotExecutionFailure.BOT_EXCEPTION,
                    response.exceptionClass(),
                    response.message()
                );
            }

            return response.decision().orElseThrow();
        } catch (SocketTimeoutException exception) {
            terminate();

            throw new BotExecutionException(
                BotExecutionFailure.TIMEOUT,
                "",
                "Bot decision exceeded "
                    + settings.decisionTimeoutMillis()
                    + " ms",
                exception
            );
        } catch (BotExecutionException exception) {
            throw exception;
        } catch (IOException | RuntimeException exception) {
            terminate();

            if (!process.isAlive()) {
                throw processFailure(
                    "Bot worker terminated during decision"
                );
            }

            throw new BotExecutionException(
                BotExecutionFailure.PROTOCOL_ERROR,
                "",
                "Invalid response from isolated bot"
                    + appendOutput(
                        processOutput.snapshot()
                    ),
                exception
            );
        } finally {
            if (!closed) {
                try {
                    socket.setSoTimeout(0);
                } catch (IOException ignored) {
                    // La connexion peut déjà être détruite.
                }
            }
        }
    }

    @Override
    public synchronized void close() {
        if (closed) {
            return;
        }

        try {
            if (process.isAlive()) {
                out.writeByte(
                    BotWireProtocol.COMMAND_CLOSE
                );
                out.flush();
            }
        } catch (IOException ignored) {
            // La destruction forcée ci-dessous reste suffisante.
        } finally {
            terminate();
        }
    }

    private void ensureOpen() {
        if (closed) {
            throw new BotExecutionException(
                BotExecutionFailure.PROCESS_FAILURE,
                "Isolated bot player is closed"
            );
        }
    }

    private BotExecutionException processFailure(
        String prefix
    ) {
        return new BotExecutionException(
            BotExecutionFailure.PROCESS_FAILURE,
            "",
            prefix
                + " (exit="
                + exitCode()
                + ")"
                + appendOutput(
                    processOutput.snapshot()
                )
        );
    }

    private int exitCode() {
        try {
            return process.exitValue();
        } catch (IllegalThreadStateException exception) {
            return Integer.MIN_VALUE;
        }
    }

    private void terminate() {
        if (closed) {
            return;
        }

        closed = true;
        closeQuietly(socket);

        if (process.isAlive()) {
            process.destroy();

            try {
                if (!process.waitFor(
                    300,
                    TimeUnit.MILLISECONDS
                )) {
                    process.destroyForcibly();
                    process.waitFor(
                        1,
                        TimeUnit.SECONDS
                    );
                }
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                process.destroyForcibly();
            }
        }
    }

    private static void validateHandshakePrefix(
        DataInputStream in,
        String expectedToken
    ) throws IOException {
        int magic = in.readInt();

        if (magic != BotWireProtocol.MAGIC) {
            throw new IOException(
                "Invalid worker protocol magic"
            );
        }

        int version = in.readInt();

        if (version != BotWireProtocol.VERSION) {
            throw new IOException(
                "Unsupported worker protocol version: "
                    + version
            );
        }

        String token =
            BotWireProtocol.readString(in);

        if (!expectedToken.equals(token)) {
            throw new IOException(
                "Worker authentication token mismatch"
            );
        }
    }

    private static ProcessBuilder createProcessBuilder(
        Class<? extends ChessBot> botClass,
        long randomSeed,
        IsolatedBotSettings settings,
        int port,
        String token
    ) {
        String javaExecutable =
            javaExecutable();

        String classPath =
            buildClassPath(botClass);

        return new ProcessBuilder(
            javaExecutable,
            "-Xmx"
                + settings.maxHeapMegabytes()
                + "m",
            "-Djava.awt.headless=true",
            "-cp",
            classPath,
            BotWorkerMain.class.getName(),
            Integer.toString(port),
            token,
            botClass.getName(),
            Long.toString(randomSeed)
        );
    }

    private static String buildClassPath(
        Class<? extends ChessBot> botClass
    ) {
        Set<String> entries =
            new LinkedHashSet<>();

        addClassPathProperty(
            entries,
            System.getProperty(
                "surefire.test.class.path",
                ""
            )
        );

        addClassPathProperty(
            entries,
            System.getProperty(
                "java.class.path",
                ""
            )
        );

        addCodeSource(
            entries,
            BotWorkerMain.class
        );
        addCodeSource(
            entries,
            ChessBot.class
        );
        addCodeSource(
            entries,
            ChessRulesEngines.class
        );
        addCodeSource(
            entries,
            botClass
        );

        try {
            addCodeSource(
                entries,
                Class.forName(
                    "io.github.wolfraam.chessgame.ChessGame"
                )
            );
        } catch (ClassNotFoundException exception) {
            throw new BotExecutionException(
                BotExecutionFailure.PROCESS_FAILURE,
                "Chess rules dependency is missing from classpath"
            );
        }

        return String.join(
            java.io.File.pathSeparator,
            entries
        );
    }

    private static void addClassPathProperty(
        Set<String> entries,
        String classPath
    ) {
        if (classPath == null
            || classPath.isBlank()) {
            return;
        }

        for (
            String entry
            : classPath.split(
                java.util.regex.Pattern.quote(
                    java.io.File.pathSeparator
                )
            )
        ) {
            if (!entry.isBlank()) {
                entries.add(entry);
            }
        }
    }

    private static void addCodeSource(
        Set<String> entries,
        Class<?> type
    ) {
        try {
            var codeSource =
                type.getProtectionDomain()
                    .getCodeSource();

            if (codeSource == null) {
                return;
            }

            URI uri =
                codeSource.getLocation()
                    .toURI();

            entries.add(
                Path.of(uri).toString()
            );
        } catch (Exception exception) {
            throw new BotExecutionException(
                BotExecutionFailure.PROCESS_FAILURE,
                "",
                "Cannot resolve classpath entry for "
                    + type.getName(),
                exception
            );
        }
    }

    private static String javaExecutable() {
        String executable =
            System.getProperty("os.name", "")
                .toLowerCase(
                    java.util.Locale.ROOT
                )
                .contains("win")
                ? "java.exe"
                : "java";

        return Path.of(
            System.getProperty("java.home"),
            "bin",
            executable
        ).toString();
    }

    private static String appendOutput(
        String output
    ) {
        if (output == null
            || output.isBlank()) {
            return "";
        }

        return " | worker output: "
            + output.replaceAll("\\s+", " ")
                .trim();
    }

    private static void closeQuietly(
        Socket socket
    ) {
        if (socket == null) {
            return;
        }

        try {
            socket.close();
        } catch (IOException ignored) {
            // best effort
        }
    }

    private static void destroy(
        Process process
    ) {
        if (process != null
            && process.isAlive()) {
            process.destroyForcibly();
        }
    }
}
