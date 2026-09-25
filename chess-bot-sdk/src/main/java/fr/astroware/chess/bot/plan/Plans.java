package fr.astroware.chess.bot.plan;

/**
 * Plans stratégiques prêts à l'emploi.
 */
public final class Plans {

    private Plans() {
    }

    public static StrategicPlan takeCenter() {
        return new TakeCenterPlan();
    }

    public static StrategicPlan developMinorPieces() {
        return new DevelopMinorPiecesPlan();
    }

    public static StrategicPlan castleKingside() {
        return new CastleKingsidePlan();
    }
}
