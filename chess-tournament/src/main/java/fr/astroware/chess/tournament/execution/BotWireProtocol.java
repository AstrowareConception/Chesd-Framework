package fr.astroware.chess.tournament.execution;

import fr.astroware.chess.bot.api.BotDecision;
import fr.astroware.chess.bot.api.BotMetadata;
import fr.astroware.chess.bot.evaluation.EvaluatedMove;
import fr.astroware.chess.bot.evaluation.EvaluationCriterion;
import fr.astroware.chess.bot.evaluation.EvaluationScore;
import fr.astroware.chess.bot.rule.AttemptStatus;
import fr.astroware.chess.bot.rule.RuleAttempt;
import fr.astroware.chess.core.model.Color;
import fr.astroware.chess.core.model.Move;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Protocole binaire interne entre le tournoi et une JVM de bot.
 *
 * <p>Il n'utilise pas la sérialisation Java : toutes les tailles sont
 * validées avant allocation afin de limiter la surface d'attaque du parent.</p>
 */
final class BotWireProtocol {

    static final int MAGIC = 0x43484657; // CHFW
    static final int VERSION = 1;

    static final byte HANDSHAKE_OK = 0;
    static final byte HANDSHAKE_ERROR = 1;

    static final byte COMMAND_DECIDE = 1;
    static final byte COMMAND_CLOSE = 2;

    static final byte RESPONSE_OK = 0;
    static final byte RESPONSE_ERROR = 1;

    private static final int MAX_STRING_BYTES = 1_048_576;
    private static final int MAX_LIST_SIZE = 10_000;

    private BotWireProtocol() {
    }

    static void writeString(
        DataOutputStream out,
        String value
    ) throws IOException {
        String normalized =
            value == null ? "" : value;

        byte[] bytes =
            normalized.getBytes(StandardCharsets.UTF_8);

        if (bytes.length > MAX_STRING_BYTES) {
            throw new IOException(
                "Protocol string exceeds "
                    + MAX_STRING_BYTES
                    + " bytes"
            );
        }

        out.writeInt(bytes.length);
        out.write(bytes);
    }

    static String readString(
        DataInputStream in
    ) throws IOException {
        int length = in.readInt();

        if (length < 0
            || length > MAX_STRING_BYTES) {
            throw new IOException(
                "Invalid protocol string length: "
                    + length
            );
        }

        byte[] bytes = new byte[length];
        in.readFully(bytes);

        return new String(
            bytes,
            StandardCharsets.UTF_8
        );
    }

    static void writeMetadata(
        DataOutputStream out,
        BotMetadata metadata
    ) throws IOException {
        writeString(out, metadata.botName());
        writeString(out, metadata.authorName());
        writeString(out, metadata.description());
    }

    static BotMetadata readMetadata(
        DataInputStream in
    ) throws IOException {
        return new BotMetadata(
            readString(in),
            readString(in),
            readString(in)
        );
    }

    static void writeMoveList(
        DataOutputStream out,
        List<Move> moves
    ) throws IOException {
        writeCount(out, moves.size());

        for (Move move : moves) {
            writeString(out, move.toUci());
        }
    }

    static List<Move> readMoveList(
        DataInputStream in
    ) throws IOException {
        int count = readCount(in);
        List<Move> moves =
            new ArrayList<>(count);

        for (int index = 0; index < count; index++) {
            moves.add(
                Move.fromUci(readString(in))
            );
        }

        return List.copyOf(moves);
    }

    static void writeDecisionRequest(
        DataOutputStream out,
        long requestId,
        Color color,
        String fen,
        List<Move> legalMoves,
        List<Move> history
    ) throws IOException {
        out.writeByte(COMMAND_DECIDE);
        out.writeLong(requestId);
        out.writeByte(color.ordinal());
        writeString(out, fen);
        writeMoveList(out, legalMoves);
        writeMoveList(out, history);
        out.flush();
    }

    static DecisionRequest readDecisionRequest(
        DataInputStream in
    ) throws IOException {
        long requestId = in.readLong();
        int colorOrdinal = in.readUnsignedByte();

        Color[] colors = Color.values();

        if (colorOrdinal >= colors.length) {
            throw new IOException(
                "Invalid color ordinal: "
                    + colorOrdinal
            );
        }

        return new DecisionRequest(
            requestId,
            colors[colorOrdinal],
            readString(in),
            readMoveList(in),
            readMoveList(in)
        );
    }

    static void writeDecisionResponse(
        DataOutputStream out,
        long requestId,
        BotDecision decision
    ) throws IOException {
        out.writeByte(RESPONSE_OK);
        out.writeLong(requestId);
        writeBotDecision(out, decision);
        out.flush();
    }

    static void writeErrorResponse(
        DataOutputStream out,
        long requestId,
        Throwable throwable
    ) throws IOException {
        out.writeByte(RESPONSE_ERROR);
        out.writeLong(requestId);
        writeString(
            out,
            throwable.getClass().getName()
        );
        writeString(
            out,
            throwable.getMessage()
        );
        out.flush();
    }

    static DecisionResponse readDecisionResponse(
        DataInputStream in
    ) throws IOException {
        int status;

        try {
            status = in.readUnsignedByte();
        } catch (EOFException exception) {
            throw new IOException(
                "Worker closed protocol stream",
                exception
            );
        }

        long requestId = in.readLong();

        if (status == RESPONSE_OK) {
            return DecisionResponse.success(
                requestId,
                readBotDecision(in)
            );
        }

        if (status == RESPONSE_ERROR) {
            return DecisionResponse.failure(
                requestId,
                readString(in),
                readString(in)
            );
        }

        throw new IOException(
            "Unknown response status: " + status
        );
    }

    private static void writeBotDecision(
        DataOutputStream out,
        BotDecision decision
    ) throws IOException {
        writeString(out, decision.move().toUci());
        writeCount(out, decision.trace().size());

        for (RuleAttempt attempt : decision.trace()) {
            writeRuleAttempt(out, attempt);
        }
    }

    private static BotDecision readBotDecision(
        DataInputStream in
    ) throws IOException {
        Move move = Move.fromUci(readString(in));

        int count = readCount(in);
        List<RuleAttempt> trace =
            new ArrayList<>(count);

        for (int index = 0; index < count; index++) {
            trace.add(readRuleAttempt(in));
        }

        return new BotDecision(
            move,
            trace
        );
    }

    private static void writeRuleAttempt(
        DataOutputStream out,
        RuleAttempt attempt
    ) throws IOException {
        writeString(out, attempt.ruleName());
        out.writeInt(attempt.status().ordinal());
        out.writeInt(attempt.detectionCount());

        writeCount(
            out,
            attempt.candidates().size()
        );

        for (EvaluatedMove candidate
            : attempt.candidates()) {
            writeEvaluatedMove(out, candidate);
        }

        out.writeBoolean(
            attempt.selectedMove().isPresent()
        );

        if (attempt.selectedMove().isPresent()) {
            writeEvaluatedMove(
                out,
                attempt.selectedMove().orElseThrow()
            );
        }

        writeString(out, attempt.explanation());
    }

    private static RuleAttempt readRuleAttempt(
        DataInputStream in
    ) throws IOException {
        String ruleName = readString(in);

        int statusOrdinal = in.readInt();
        AttemptStatus[] statuses =
            AttemptStatus.values();

        if (statusOrdinal < 0
            || statusOrdinal >= statuses.length) {
            throw new IOException(
                "Invalid attempt status ordinal: "
                    + statusOrdinal
            );
        }

        int detectionCount = in.readInt();

        if (detectionCount < 0) {
            throw new IOException(
                "Negative detection count"
            );
        }

        int candidateCount = readCount(in);
        List<EvaluatedMove> candidates =
            new ArrayList<>(candidateCount);

        for (
            int index = 0;
            index < candidateCount;
            index++
        ) {
            candidates.add(
                readEvaluatedMove(in)
            );
        }

        Optional<EvaluatedMove> selected =
            in.readBoolean()
                ? Optional.of(
                    readEvaluatedMove(in)
                )
                : Optional.empty();

        return new RuleAttempt(
            ruleName,
            statuses[statusOrdinal],
            detectionCount,
            candidates,
            selected,
            readString(in)
        );
    }

    private static void writeEvaluatedMove(
        DataOutputStream out,
        EvaluatedMove move
    ) throws IOException {
        writeString(out, move.move().toUci());
        out.writeDouble(move.score().value());
        out.writeDouble(move.aggression().value());
        out.writeDouble(move.safety().value());
        out.writeDouble(move.risk().value());

        writeCount(out, move.criteria().size());

        for (EvaluationCriterion criterion
            : move.criteria()) {
            writeString(out, criterion.name());
            out.writeDouble(
                criterion.score().value()
            );
            out.writeDouble(criterion.weight());
            writeString(
                out,
                criterion.explanation()
            );
        }

        writeString(out, move.explanation());
    }

    private static EvaluatedMove readEvaluatedMove(
        DataInputStream in
    ) throws IOException {
        Move move = Move.fromUci(readString(in));

        double score = readScore(in);
        double aggression = readScore(in);
        double safety = readScore(in);
        double risk = readScore(in);

        int criterionCount = readCount(in);

        List<EvaluationCriterion> criteria =
            new ArrayList<>(criterionCount);

        for (
            int index = 0;
            index < criterionCount;
            index++
        ) {
            criteria.add(
                new EvaluationCriterion(
                    readString(in),
                    EvaluationScore.of(
                        readScore(in)
                    ),
                    readWeight(in),
                    readString(in)
                )
            );
        }

        return new EvaluatedMove(
            move,
            EvaluationScore.of(score),
            EvaluationScore.of(aggression),
            EvaluationScore.of(safety),
            EvaluationScore.of(risk),
            criteria,
            readString(in)
        );
    }

    private static double readScore(
        DataInputStream in
    ) throws IOException {
        double value = in.readDouble();

        if (!Double.isFinite(value)
            || value < 0.0
            || value > 10.0) {
            throw new IOException(
                "Invalid score: " + value
            );
        }

        return value;
    }

    private static double readWeight(
        DataInputStream in
    ) throws IOException {
        double value = in.readDouble();

        if (!Double.isFinite(value)
            || value < 0.0) {
            throw new IOException(
                "Invalid criterion weight: "
                    + value
            );
        }

        return value;
    }

    private static void writeCount(
        DataOutputStream out,
        int count
    ) throws IOException {
        if (count < 0
            || count > MAX_LIST_SIZE) {
            throw new IOException(
                "Invalid protocol list size: "
                    + count
            );
        }

        out.writeInt(count);
    }

    private static int readCount(
        DataInputStream in
    ) throws IOException {
        int count = in.readInt();

        if (count < 0
            || count > MAX_LIST_SIZE) {
            throw new IOException(
                "Invalid protocol list size: "
                    + count
            );
        }

        return count;
    }

    record DecisionRequest(
        long requestId,
        Color color,
        String fen,
        List<Move> legalMoves,
        List<Move> history
    ) {
    }

    record DecisionResponse(
        long requestId,
        Optional<BotDecision> decision,
        String exceptionClass,
        String message
    ) {
        static DecisionResponse success(
            long requestId,
            BotDecision decision
        ) {
            return new DecisionResponse(
                requestId,
                Optional.of(decision),
                "",
                ""
            );
        }

        static DecisionResponse failure(
            long requestId,
            String exceptionClass,
            String message
        ) {
            return new DecisionResponse(
                requestId,
                Optional.empty(),
                exceptionClass,
                message
            );
        }

        boolean success() {
            return decision.isPresent();
        }
    }
}
