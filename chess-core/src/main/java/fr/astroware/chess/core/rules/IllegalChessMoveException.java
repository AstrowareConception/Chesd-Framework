package fr.astroware.chess.core.rules;

import fr.astroware.chess.core.model.Move;

/**
 * Exception du domaine levée lorsqu'un coup ne fait pas partie des coups
 * légaux de la position.
 */
public final class IllegalChessMoveException extends RuntimeException {

    public IllegalChessMoveException(Move move) {
        super("Illegal chess move: " + move.toUci());
    }
}
