package fr.astroware.chess.bot.action;

import fr.astroware.chess.bot.evaluation.EvaluatedMove;
import fr.astroware.chess.bot.rule.Action;
import fr.astroware.chess.bot.rule.Detection;
import fr.astroware.chess.core.model.Move;

import java.util.List;

/**
 * Point d'entrée vers les actions réutilisables fournies par le framework.
 */
public final class Actions {

    private Actions() {
    }

    /**
     * Choisit aléatoirement un coup parmi les coups légaux.
     *
     * <p>Cette action retourne un seul candidat avec une note neutre de 5/10 :
     * elle ne prétend pas savoir si le coup est bon, seulement qu'il est
     * légal. Le générateur pseudo-aléatoire vient du contexte afin que le
     * tournoi puisse reproduire une partie à partir de sa graine.</p>
     */
    public static <D extends Detection> Action<D> randomLegalMove() {
        return (context, detections) -> {
            List<Move> legalMoves = context.legalMoves();

            if (legalMoves.isEmpty()) {
                return List.of();
            }

            Move move = legalMoves.get(
                context.random().nextInt(legalMoves.size())
            );

            return List.of(
                EvaluatedMove.of(
                    move,
                    5.0,
                    "Coup légal choisi aléatoirement"
                )
            );
        };
    }
}
