package fr.astroware.chess.bot.rule;

/**
 * Résultat de l'évaluation d'une règle.
 */
public enum AttemptStatus {
    NOT_MATCHED,
    MATCHED_NO_MOVE,
    ILLEGAL_PROPOSAL,
    SELECTED,
    ERROR
}
