package fr.astroware.chess.core.model;

/**
 * Couleur d'un camp aux échecs.
 *
 * <p>Ce type est volontairement un enum : il n'existe que deux valeurs
 * possibles et l'objet est naturellement immuable.</p>
 */
public enum Color {
    WHITE,
    BLACK;

    /**
     * Retourne la couleur adverse.
     *
     * @return WHITE si la couleur courante est BLACK, BLACK sinon.
     */
    public Color opposite() {
        return this == WHITE ? BLACK : WHITE;
    }
}
