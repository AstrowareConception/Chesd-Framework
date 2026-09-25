package fr.astroware.chess.bot.analysis;

import fr.astroware.chess.bot.api.BotContext;
import fr.astroware.chess.core.model.Color;
import fr.astroware.chess.core.model.Move;
import fr.astroware.chess.core.model.PlacedPiece;
import fr.astroware.chess.core.model.Square;

import java.util.List;

/**
 * Façade d'analyse de la position mise à disposition des bots.
 *
 * <p>Cette couche expose des faits réutilisables. Elle ne décide pas de la
 * stratégie à adopter : c'est le rôle des règles, actions, plans et profils.</p>
 */
public interface Analysis {

    AttackMap attackMap();

    List<PlacedPiece> attackersOf(Square square, Color color);

    List<PlacedPiece> defendersOf(PlacedPiece piece);

    boolean isAttacked(PlacedPiece piece);

    boolean isDefended(PlacedPiece piece);

    boolean isHanging(PlacedPiece piece);

    List<PlacedPiece> hangingPieces(Color color);

    int material(Color color);

    MaterialBalance materialBalance();

    List<Move> captures();

    PieceValues pieceValues();

    /**
     * Simule un coup légal et retourne une nouvelle position entièrement
     * analysable, sans modifier la partie réelle.
     */
    PositionProjection after(Move move);

    static Analysis of(BotContext context) {
        return new DefaultAnalysis(context);
    }
}
