package fr.astroware.chess.bot.rule;

import fr.astroware.chess.bot.api.BotContext;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Détecte une situation dans la position courante.
 *
 * @param <D> type d'information produit par la détection
 */
@FunctionalInterface
public interface Situation<D extends Detection> {

    /**
     * Recherche toutes les occurrences de la situation.
     *
     * @param context contexte de décision du bot
     * @return liste éventuellement vide des détections
     */
    List<D> detect(BotContext context);

    default boolean matches(BotContext context) {
        return !detect(context).isEmpty();
    }

    /**
     * Crée une situation dérivée qui conserve uniquement certaines détections.
     *
     * <p>Ce mécanisme permettra par exemple d'exprimer :
     * « une pièce pendue dont la valeur est au moins celle d'une tour ».</p>
     */
    default Situation<D> filter(Predicate<? super D> predicate) {
        Objects.requireNonNull(predicate, "predicate must not be null");

        return context -> detect(context)
            .stream()
            .filter(predicate)
            .toList();
    }
}
