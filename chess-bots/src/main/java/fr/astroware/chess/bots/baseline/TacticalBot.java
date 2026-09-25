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
 * <p>Sa classe est volontairement lisible comme une liste de priorités. Elle
 * sert d'exemple aux étudiants pour montrer qu'un comportement assez riche
 * peut être construit par composition de situations et d'actions.</p>
 */
public final class TacticalBot extends ChessBot {

    @Override
    public BotMetadata metadata() {
        return new BotMetadata(
            "Tactical Bot",
            "AstroWare Conception",
            "Bot tactique : mat, défense, fourchette, clouage, enfilade et échecs."
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
                "Sortir d'échec",
                Situations.inCheck(),
                Actions.bestCheckEscape()
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
                "Créer un clouage",
                Situations.pinOpportunity(),
                Actions.playBestPin()
            ),
            rule(
                "Créer une enfilade",
                Situations.skewerOpportunity(),
                Actions.playBestSkewer()
            ),
            rule(
                "Donner échec",
                Situations.checkAvailable(),
                Actions.playBestCheck()
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
