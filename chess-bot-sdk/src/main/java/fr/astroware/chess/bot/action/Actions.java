package fr.astroware.chess.bot.action;

import fr.astroware.chess.bot.rule.Action;
import fr.astroware.chess.bot.rule.Detection;
import fr.astroware.chess.core.model.Move;

import java.util.List;
import java.util.Optional;

/**
 * Point d'entrée vers les actions réutilisables fournies par le framework.
 */
public final class Actions {

    private Actions() {
    }

    /**
     * Choisit aléatoirement un coup parmi les coups légaux.
     *
     * <p>Le générateur pseudo-aléatoire vient du contexte afin que le moteur de
     * tournoi puisse fournir une graine et reproduire une partie.</p>
     */
    public static <D extends Detection> Action<D> randomLegalMove() {
        return (context, detections) -> {
            List<Move> legalMoves = context.legalMoves();

            if (legalMoves.isEmpty()) {
                return Optional.empty();
            }

            return Optional.of(
                legalMoves.get(context.random().nextInt(legalMoves.size()))
            );
        };
    }
}
