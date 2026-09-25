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
 * Bot tactique de référence.
 *
 * <p>Il illustre la progression naturelle du framework : les mêmes règles
 * simples restent lisibles, mais certaines situations utilisent désormais la
 * projection de position pour détecter des motifs plus riches.</p>
 */
public final class TacticalBot extends ChessBot {

    @Override
    public BotMetadata metadata() {
        return new BotMetadata(
            "Tactical Bot",
            "AstroWare Conception",
            "Bot de référence : mat immédiat, sécurité, prises et fourchettes."
        );
    }

    @Override
    protected StrategyProfile strategyProfile() {
        return StrategyProfiles.aggressive();
    }

    @Override
    protected List<Rule<?>> rules() {
        return List.of(
            rule(
                "Mater immédiatement",
                Situations.mateInOne(),
                Actions.playMateInOne()
            ),
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
                "Créer une fourchette",
                Situations.forkOpportunity(),
                Actions.playBestFork()
            ),
            rule(
                "Capture tactique",
                Situations.captureAvailable(),
                Actions.captureWithRiskAwareness()
            ),
            Plans.takeCenter().asRule(),
            Plans.developMinorPieces().asRule(),
            Plans.castleKingside().asRule(),
            rule(
                "Secours",
                Situations.always(),
                Actions.randomLegalMove()
            )
        );
    }
}
