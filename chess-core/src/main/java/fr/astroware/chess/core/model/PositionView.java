package fr.astroware.chess.core.model;

import java.util.List;
import java.util.Optional;

/**
 * Vue en lecture seule d'une position.
 *
 * <p>Les bots reçoivent une vue, et non l'état mutable du moteur. Cette
 * séparation protège l'encapsulation du framework : un bot observe la partie,
 * mais ne peut pas déplacer directement les pièces.</p>
 */
public interface PositionView {

    Optional<Piece> pieceAt(Square square);

    List<PlacedPiece> pieces();

    List<PlacedPiece> pieces(Color color);

    Color sideToMove();

    Optional<Move> lastMove();

    String fen();
}
