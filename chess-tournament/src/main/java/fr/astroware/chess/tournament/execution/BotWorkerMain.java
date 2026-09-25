package fr.astroware.chess.tournament.execution;

import fr.astroware.chess.bot.api.BotContext;
import fr.astroware.chess.bot.api.BotDecision;
import fr.astroware.chess.bot.api.BotMetadata;
import fr.astroware.chess.bot.api.ChessBot;
import fr.astroware.chess.core.model.Color;
import fr.astroware.chess.core.model.Move;
import fr.astroware.chess.core.model.PositionView;
import fr.astroware.chess.core.rules.ChessRulesEngine;
import fr.astroware.chess.core.rules.ChessRulesEngines;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;
import java.util.Random;
import java.util.random.RandomGenerator;

/**
 * Point d'entrée d'une JVM enfant hébergeant un bot.
 *
 * <p>Le protocole passe exclusivement par un socket loopback. stdout/stderr
 * restent donc libres pour le code étudiant sans corrompre les messages du
 * tournoi.</p>
 */
public final class BotWorkerMain {

    private BotWorkerMain() {
    }

    public static void main(String[] args)
        throws Exception {

        if (args.length != 4) {
            System.err.println(
                "Usage: BotWorkerMain <port> <token> <botClass> <seed>"
            );
            System.exit(2);
        }

        int port = Integer.parseInt(args[0]);
        String token = args[1];
        String botClassName = args[2];
        long seed = Long.parseLong(args[3]);

        try (
            Socket socket = new Socket(
                InetAddress.getLoopbackAddress(),
                port
            );
            DataInputStream in =
                new DataInputStream(
                    socket.getInputStream()
                );
            DataOutputStream out =
                new DataOutputStream(
                    socket.getOutputStream()
                )
        ) {
            socket.setTcpNoDelay(true);

            out.writeInt(BotWireProtocol.MAGIC);
            out.writeInt(BotWireProtocol.VERSION);
            BotWireProtocol.writeString(
                out,
                token
            );
            out.flush();

            ChessBot bot;

            try {
                bot = instantiate(botClassName);

                BotMetadata metadata =
                    java.util.Objects.requireNonNull(
                        bot.metadata(),
                        "bot metadata must not be null"
                    );

                out.writeByte(
                    BotWireProtocol.HANDSHAKE_OK
                );
                BotWireProtocol.writeMetadata(
                    out,
                    metadata
                );
                out.flush();
            } catch (Throwable throwable) {
                out.writeByte(
                    BotWireProtocol.HANDSHAKE_ERROR
                );
                BotWireProtocol.writeString(
                    out,
                    throwable.getClass().getName()
                );
                BotWireProtocol.writeString(
                    out,
                    throwable.getMessage()
                );
                out.flush();
                return;
            }

            ChessRulesEngine engine =
                ChessRulesEngines.standard();

            RandomGenerator random =
                new Random(seed);

            while (true) {
                int command = in.readUnsignedByte();

                if (command
                    == BotWireProtocol.COMMAND_CLOSE) {
                    return;
                }

                if (command
                    != BotWireProtocol.COMMAND_DECIDE) {
                    throw new IllegalStateException(
                        "Unknown worker command: "
                            + command
                    );
                }

                BotWireProtocol.DecisionRequest request =
                    BotWireProtocol.readDecisionRequest(
                        in
                    );

                try {
                    PositionView position =
                        engine.fromFen(
                            request.fen()
                        );

                    if (position.sideToMove()
                        != request.color()) {
                        throw new IllegalArgumentException(
                            "Request color does not match FEN side to move"
                        );
                    }

                    BotContext context =
                        new WorkerContext(
                            request.color(),
                            position,
                            request.legalMoves(),
                            request.history(),
                            random
                        );

                    BotDecision decision =
                        bot.decide(context);

                    BotWireProtocol.writeDecisionResponse(
                        out,
                        request.requestId(),
                        decision
                    );
                } catch (Throwable throwable) {
                    BotWireProtocol.writeErrorResponse(
                        out,
                        request.requestId(),
                        throwable
                    );
                }
            }
        }
    }

    private static ChessBot instantiate(
        String className
    ) throws Exception {
        Class<?> rawClass =
            Class.forName(className);

        if (!ChessBot.class.isAssignableFrom(
            rawClass
        )) {
            throw new IllegalArgumentException(
                className
                    + " does not extend ChessBot"
            );
        }

        @SuppressWarnings("unchecked")
        Class<? extends ChessBot> botClass =
            (Class<? extends ChessBot>) rawClass;

        Constructor<? extends ChessBot> constructor =
            botClass.getDeclaredConstructor();

        if (!Modifier.isPublic(
            constructor.getModifiers()
        )) {
            throw new IllegalArgumentException(
                "Bot constructor must be public and parameterless: "
                    + className
            );
        }

        return constructor.newInstance();
    }

    private record WorkerContext(
        Color myColor,
        PositionView position,
        List<Move> legalMoves,
        List<Move> moveHistory,
        RandomGenerator random
    ) implements BotContext {

        private WorkerContext {
            legalMoves = List.copyOf(legalMoves);
            moveHistory = List.copyOf(moveHistory);
        }
    }
}
