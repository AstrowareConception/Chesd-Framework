package fr.astroware.chess.tournament.pgn;

import fr.astroware.chess.core.model.Color;
import fr.astroware.chess.core.rules.ChessRulesEngines;
import fr.astroware.chess.tournament.match.MatchResult;
import fr.astroware.chess.tournament.match.PlayedMove;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

/**
 * Exporte un MatchResult au format PGN standard.
 *
 * <p>Le texte produit peut être enregistré dans un fichier .pgn puis ouvert
 * dans un viewer compatible.</p>
 */
public final class PgnExporter {

    private static final DateTimeFormatter PGN_DATE =
        DateTimeFormatter.ofPattern("yyyy.MM.dd");

    public String export(MatchResult result) {
        Objects.requireNonNull(result, "result must not be null");

        StringBuilder pgn = new StringBuilder();

        tag(pgn, "Event", "Chess Framework Bot Match");
        tag(pgn, "Site", "Chess Framework");
        tag(
            pgn,
            "Date",
            LocalDate.now(ZoneOffset.UTC).format(PGN_DATE)
        );
        tag(pgn, "Round", "?");
        tag(pgn, "White", result.white().botName());
        tag(pgn, "Black", result.black().botName());
        tag(pgn, "WhiteAuthor", result.white().authorName());
        tag(pgn, "BlackAuthor", result.black().authorName());
        tag(pgn, "Result", result.pgnResult());
        tag(pgn, "PlyCount", Integer.toString(result.pliesPlayed()));
        tag(
            pgn,
            "Termination",
            result.termination().name().toLowerCase(
                java.util.Locale.ROOT
            )
        );

        result.incident().ifPresent(incident -> {
            tag(
                pgn,
                "ForfeitBy",
                incident.offender().botName()
            );
            tag(
                pgn,
                "ForfeitReason",
                incident.type().name()
            );
        });

        String standardFen = ChessRulesEngines.standard()
            .initialPosition()
            .fen();

        if (!standardFen.equals(result.initialFen())) {
            tag(pgn, "SetUp", "1");
            tag(pgn, "FEN", result.initialFen());
        }

        pgn.append(System.lineSeparator());

        appendMoveText(pgn, result);
        pgn.append(' ').append(result.pgnResult());
        pgn.append(System.lineSeparator());

        return pgn.toString();
    }

    private static void appendMoveText(
        StringBuilder pgn,
        MatchResult result
    ) {
        FenStart start = FenStart.parse(result.initialFen());
        int moveNumber = start.fullMoveNumber();
        boolean needBlackEllipsis = start.sideToMove() == Color.BLACK;

        List<PlayedMove> moves = result.playedMoves();

        for (int index = 0; index < moves.size(); index++) {
            PlayedMove move = moves.get(index);

            if (move.color() == Color.WHITE) {
                appendToken(pgn, moveNumber + ".");
                appendToken(pgn, move.san());
                needBlackEllipsis = false;
            } else {
                if (needBlackEllipsis) {
                    appendToken(pgn, moveNumber + "...");
                }

                appendToken(pgn, move.san());
                moveNumber++;
                needBlackEllipsis = true;
            }
        }
    }

    private static void appendToken(StringBuilder builder, String token) {
        if (!builder.isEmpty()
            && !Character.isWhitespace(builder.charAt(builder.length() - 1))) {
            builder.append(' ');
        }

        builder.append(token);
    }

    private static void tag(
        StringBuilder builder,
        String name,
        String value
    ) {
        builder.append('[')
            .append(name)
            .append(" \"")
            .append(escape(value))
            .append("\"]")
            .append(System.lineSeparator());
    }

    private static String escape(String value) {
        return value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"");
    }

    private record FenStart(Color sideToMove, int fullMoveNumber) {

        static FenStart parse(String fen) {
            String[] fields = fen.trim().split("\\s+");

            if (fields.length < 6) {
                return new FenStart(Color.WHITE, 1);
            }

            Color color = "b".equals(fields[1])
                ? Color.BLACK
                : Color.WHITE;

            int moveNumber;
            try {
                moveNumber = Integer.parseInt(fields[5]);
            } catch (NumberFormatException exception) {
                moveNumber = 1;
            }

            return new FenStart(color, Math.max(1, moveNumber));
        }
    }
}
