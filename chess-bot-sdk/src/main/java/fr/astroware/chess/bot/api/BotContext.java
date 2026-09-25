package fr.astroware.chess.bot.api;

import fr.astroware.chess.bot.analysis.Analysis;
import fr.astroware.chess.core.model.Color;
import fr.astroware.chess.core.model.Move;
import fr.astroware.chess.core.model.PositionView;

import java.util.List;
import java.util.random.RandomGenerator;

/**
 * Informations accessibles à un bot au moment où il doit jouer.
 *
 * <p>Le contexte est en lecture seule. Le bot peut observer la position et
 * choisir parmi les coups légaux, mais il ne peut pas modifier directement
 * l'état interne de la partie.</p>
 */
public interface BotContext {

    Color myColor();

    PositionView position();

    List<Move> legalMoves();

    /**
     * Historique complet des demi-coups depuis le début de la partie.
     *
     * <p>La valeur par défaut permet aux contextes de test très simples de ne
     * pas avoir à simuler tout un historique. Le moteur réel fournira cette
     * information.</p>
     */
    default List<Move> moveHistory() {
        return List.of();
    }

    /**
     * Façade d'analyse de la position courante.
     *
     * <p>La première implémentation est construite à la demande. Le moteur de
     * tournoi pourra plus tard fournir une version mise en cache si les
     * analyses deviennent coûteuses.</p>
     */
    default Analysis analysis() {
        return Analysis.of(this);
    }

    RandomGenerator random();
}
