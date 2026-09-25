package fr.astroware.chess.core.rules;

import fr.astroware.chess.core.model.Move;
import fr.astroware.chess.core.model.PieceType;
import fr.astroware.chess.core.model.PositionView;
import fr.astroware.chess.core.model.Square;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChessRulesSpecialMovesTest {

    private final ChessRulesEngine engine =
        ChessRulesEngines.standard();

    @Test
    void supportsKingSideAndQueenSideCastling() {
        PositionView position = engine.fromFen(
            "r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1"
        );

        Set<Move> legalMoves =
            Set.copyOf(engine.legalMoves(position));

        assertTrue(
            legalMoves.contains(
                Move.fromUci("e1g1")
            )
        );
        assertTrue(
            legalMoves.contains(
                Move.fromUci("e1c1")
            )
        );

        assertEquals(
            "O-O",
            engine.toSan(
                position,
                Move.fromUci("e1g1")
            )
        );

        assertEquals(
            "O-O-O",
            engine.toSan(
                position,
                Move.fromUci("e1c1")
            )
        );

        PositionView kingSide =
            engine.play(
                position,
                Move.fromUci("e1g1")
            );

        assertEquals(
            PieceType.KING,
            kingSide.pieceAt(
                Square.from("g1")
            ).orElseThrow().type()
        );
        assertEquals(
            PieceType.ROOK,
            kingSide.pieceAt(
                Square.from("f1")
            ).orElseThrow().type()
        );

        PositionView queenSide =
            engine.play(
                position,
                Move.fromUci("e1c1")
            );

        assertEquals(
            PieceType.KING,
            queenSide.pieceAt(
                Square.from("c1")
            ).orElseThrow().type()
        );
        assertEquals(
            PieceType.ROOK,
            queenSide.pieceAt(
                Square.from("d1")
            ).orElseThrow().type()
        );
    }

    @Test
    void supportsEnPassantCapture() {
        PositionView position = engine.fromFen(
            "4k3/8/8/3pP3/8/8/8/4K3 w - d6 0 1"
        );

        Move enPassant =
            Move.fromUci("e5d6");

        assertTrue(
            engine.legalMoves(position)
                .contains(enPassant)
        );

        PositionView after =
            engine.play(
                position,
                enPassant
            );

        assertEquals(
            PieceType.PAWN,
            after.pieceAt(
                Square.from("d6")
            ).orElseThrow().type()
        );

        assertTrue(
            after.pieceAt(
                Square.from("d5")
            ).isEmpty()
        );
        assertTrue(
            after.pieceAt(
                Square.from("e5")
            ).isEmpty()
        );
    }

    @Test
    void exposesAllFourPromotionChoices() {
        PositionView position = engine.fromFen(
            "4k3/P7/8/8/8/8/8/4K3 w - - 0 1"
        );

        Set<String> promotions =
            engine.legalMoves(position)
                .stream()
                .filter(move ->
                    move.from().equals(
                        Square.from("a7")
                    )
                )
                .filter(move ->
                    move.to().equals(
                        Square.from("a8")
                    )
                )
                .map(Move::toUci)
                .collect(Collectors.toSet());

        assertEquals(
            Set.of(
                "a7a8q",
                "a7a8r",
                "a7a8b",
                "a7a8n"
            ),
            promotions
        );

        PositionView after =
            engine.play(
                position,
                Move.fromUci("a7a8q")
            );

        assertEquals(
            PieceType.QUEEN,
            after.pieceAt(
                Square.from("a8")
            ).orElseThrow().type()
        );
    }
}
