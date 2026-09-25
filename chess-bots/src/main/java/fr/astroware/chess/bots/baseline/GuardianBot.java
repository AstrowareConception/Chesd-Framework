package fr.astroware.chess.bots.baseline;

import fr.astroware.chess.bot.action.Actions;
import fr.astroware.chess.bot.api.BotMetadata;
import fr.astroware.chess.bot.api.ChessBot;
import fr.astroware.chess.bot.plan.Plans;
import fr.astroware.chess.bot.rule.Rule;
import fr.astroware.chess.bot.situation.Situations;
import fr.astroware.chess.bot.strategy.StrategyProfile;
import fr.astroware.chess.bot.strategy.StrategyProfiles;

import java.util.List;

/**
 * Bot défensif utilisant la projection de position.
 *
 * <p>GuardianBot cherche en priorité à sauver une pièce alliée pendue. Chaque
 * case de fuite est simulée avec le vrai moteur avant d'être évaluée.</p>
 */
public final class GuardianBot extends ChessBot {

    @Override
    public BotMetadata metadata() {
        return new BotMetadata(
            "Guardian",
            "AstroWare Conception",
            "Bot défensif : protège d'abord ses pièces menacées."
        );
    }

    @Override
    protected StrategyProfile strategyProfile() {
        return StrategyProfiles.defensive();
    }

    @Override
    protected List<Rule<?>> rules() {
        return List.of(
            rule(
                "Sauver une pièce pendue",
                Situations.hangingOwnPiece(),
                Actions.moveThreatenedPieceToSafety()
            ),
            rule(
                "Prendre une pièce pendue",
                Situations.hangingEnemyPiece(),
                Actions.captureHighestValue()
            ),
            rule(
                "Capture prudente",
                Situations.captureAvailable(),
                Actions.captureWithRiskAwareness()
            ),
            Plans.castleKingside().asRule(),
            Plans.developMinorPieces().asRule(),
            Plans.takeCenter().asRule(),
            rule(
                "Secours",
                Situations.always(),
                Actions.randomLegalMove()
            )
        );
    }
}
