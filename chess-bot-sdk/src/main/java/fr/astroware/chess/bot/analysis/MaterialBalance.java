package fr.astroware.chess.bot.analysis;

import fr.astroware.chess.core.model.Color;

/**
 * Résumé du matériel d'une position.
 *
 * @param white matériel blanc
 * @param black matériel noir
 */
public record MaterialBalance(int white, int black) {

    /**
     * Différence matérielle du point de vue indiqué.
     */
    public int advantageFor(Color color) {
        return color == Color.WHITE
            ? white - black
            : black - white;
    }
}
