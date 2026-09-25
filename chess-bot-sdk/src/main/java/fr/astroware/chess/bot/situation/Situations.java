package fr.astroware.chess.bot.situation;

import fr.astroware.chess.bot.rule.PresenceDetection;
import fr.astroware.chess.bot.rule.Situation;

import java.util.List;

/**
 * Point d'entrée vers les situations fournies par le framework.
 *
 * <p>Le catalogue grandira progressivement. Cette première situation sert
 * notamment à exprimer une règle de secours.</p>
 */
public final class Situations {

    private Situations() {
    }

    /**
     * Situation toujours reconnue.
     */
    public static Situation<PresenceDetection> always() {
        return context -> List.of(PresenceDetection.INSTANCE);
    }
}
