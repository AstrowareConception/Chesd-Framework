package fr.astroware.chess.core.rules;

import fr.astroware.chess.core.game.GameResult;
import fr.astroware.chess.core.model.Move;
import fr.astroware.chess.core.model.PositionView;

import java.util.List;

/**
 * Abstraction du moteur de règles d'échecs.
 *
 * <p>Cette interface est volontairement indépendante de toute bibliothèque
 * tierce. Le reste du framework dépend de ce contrat et non de l'implémentation
 * choisie.</p>
 */
public interface ChessRulesEngine {

    PositionView initialPosition();

    PositionView fromFen(String fen);

    List<Move> legalMoves(PositionView position);

    /**
     * Retourne la notation algébrique courte (SAN) du coup dans la position
     * courante : e4, Nf3, O-O, Qh4#, etc.
     */
    String toSan(PositionView position, Move move);

    PositionView play(PositionView position, Move move);

    GameResult result(PositionView position);

    boolean isKingAttacked(PositionView position);
}
