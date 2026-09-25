package fr.astroware.chess.bot.search;

/**
 * Paramètres d'une recherche adversariale.
 *
 * @param depth profondeur totale en demi-coups, coup racine inclus
 * @param maxMovesPerNode nombre maximal de coups explorés par nœud
 * @param alphaBeta active l'élagage alpha-bêta
 */
public record SearchSettings(
    int depth,
    int maxMovesPerNode,
    boolean alphaBeta
) {

    public SearchSettings {
        if (depth <= 0) {
            throw new IllegalArgumentException(
                "depth must be > 0"
            );
        }

        if (maxMovesPerNode <= 0) {
            throw new IllegalArgumentException(
                "maxMovesPerNode must be > 0"
            );
        }
    }

    /**
     * Recherche exhaustive jusqu'à la profondeur demandée.
     */
    public static SearchSettings exact(int depth) {
        return new SearchSettings(
            depth,
            Integer.MAX_VALUE,
            true
        );
    }

    /**
     * Recherche bornée, adaptée aux démonstrations et au tournoi.
     */
    public static SearchSettings bounded(
        int depth,
        int maxMovesPerNode
    ) {
        return new SearchSettings(
            depth,
            maxMovesPerNode,
            true
        );
    }

    public SearchSettings withoutAlphaBeta() {
        return new SearchSettings(
            depth,
            maxMovesPerNode,
            false
        );
    }
}
