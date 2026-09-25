package fr.astroware.chess.bot.analysis;

import fr.astroware.chess.core.model.Color;
import fr.astroware.chess.core.model.PlacedPiece;

import java.util.List;
import java.util.Objects;

/**
 * Analyse des principaux motifs de structure de pions d'un camp.
 *
 * @param color camp analysé
 * @param pawns tous les pions du camp
 * @param isolated pions sans pion allié sur les colonnes adjacentes
 * @param doubled pions partageant une colonne avec au moins un autre pion allié
 * @param passed pions sans pion adverse devant eux sur leur colonne ou une colonne adjacente
 * @param protectedPassed pions passés défendus par un pion allié
 */
public record PawnStructure(
    Color color,
    List<PlacedPiece> pawns,
    List<PlacedPiece> isolated,
    List<PlacedPiece> doubled,
    List<PlacedPiece> passed,
    List<PlacedPiece> protectedPassed
) {

    public PawnStructure {
        Objects.requireNonNull(color, "color must not be null");
        pawns = List.copyOf(Objects.requireNonNull(pawns));
        isolated = List.copyOf(Objects.requireNonNull(isolated));
        doubled = List.copyOf(Objects.requireNonNull(doubled));
        passed = List.copyOf(Objects.requireNonNull(passed));
        protectedPassed = List.copyOf(Objects.requireNonNull(protectedPassed));
    }

    public int isolatedCount() {
        return isolated.size();
    }

    public int doubledCount() {
        return doubled.size();
    }

    public int passedCount() {
        return passed.size();
    }
}
