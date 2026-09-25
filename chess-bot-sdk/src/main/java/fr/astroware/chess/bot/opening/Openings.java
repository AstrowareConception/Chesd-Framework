package fr.astroware.chess.bot.opening;

import fr.astroware.chess.core.model.Color;

import java.util.List;

/**
 * Petit répertoire d'ouvertures fourni avec le framework.
 *
 * <p>Il ne s'agit pas d'une encyclopédie. Ces lignes servent de briques
 * pédagogiques que les étudiants peuvent utiliser, enrichir ou remplacer par
 * leur propre répertoire.</p>
 */
public final class Openings {

    private Openings() {
    }

    /**
     * Répertoire blanc du système de Londres.
     */
    public static OpeningBook londonSystem() {
        return new OpeningBook(
            "Système de Londres",
            Color.WHITE,
            List.of(
                OpeningLine.styled(
                    "Structure classique",
                    4.0,
                    8.0,
                    2.0,
                    "d2d4", "d7d5",
                    "g1f3", "g8f6",
                    "c1f4", "e7e6",
                    "e2e3", "f8d6",
                    "f4g3", "e8g8",
                    "f1d3"
                ),
                OpeningLine.styled(
                    "Développement contre ...Nf6 et ...e6",
                    4.0,
                    8.0,
                    2.0,
                    "d2d4", "g8f6",
                    "g1f3", "e7e6",
                    "c1f4", "b7b6",
                    "e2e3", "c8b7",
                    "f1d3"
                ),
                OpeningLine.styled(
                    "Réponse à ...c5",
                    5.0,
                    7.0,
                    3.0,
                    "d2d4", "d7d5",
                    "c1f4", "g8f6",
                    "e2e3", "c7c5",
                    "c2c3", "b8c6",
                    "g1f3"
                )
            )
        );
    }

    /**
     * Répertoire noir de défense Scandinave.
     */
    public static OpeningBook scandinavianDefense() {
        return new OpeningBook(
            "Défense Scandinave",
            Color.BLACK,
            List.of(
                OpeningLine.styled(
                    "Variante avec reprise de la dame",
                    7.0,
                    4.0,
                    6.0,
                    "e2e4", "d7d5",
                    "e4d5", "d8d5",
                    "b1c3", "d5a5",
                    "d2d4", "g8f6",
                    "g1f3", "c7c6"
                ),
                OpeningLine.styled(
                    "Scandinave moderne ...Nf6",
                    6.0,
                    6.0,
                    5.0,
                    "e2e4", "d7d5",
                    "e4d5", "g8f6",
                    "d2d4", "f6d5",
                    "g1f3"
                )
            )
        );
    }
}
