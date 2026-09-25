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

    public static StrategicPlan createPassedPawn() {
        return new CreatePassedPawnPlan();
    }

    public static StrategicPlan useOpenFile() {
        return new UseOpenFilePlan();
    }

    public static StrategicPlan improveKingSafety() {
        return new ImproveKingSafetyPlan();
    }
}
