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
 * Bot de référence orienté pression.
 *
 * <p>PressureBot privilégie les contraintes positionnelles et tactiques :
 * surcharge d'un défenseur, clouage, enfilade et attaque à la découverte.
 * Il illustre une approche différente d'un bot purement matérialiste.</p>
 */
public final class PressureBot extends ChessBot {

    @Override
    public BotMetadata metadata() {
        return new BotMetadata(
            "Pressure Bot",
            "AstroWare Conception",
            "Bot de pression : surcharge les défenseurs et multiplie les contraintes."
        );
    }

    @Override
    protected StrategyProfile strategyProfile() {
        return StrategyProfiles.profile(
            "Pression",
            8.5,
            5.0,
            6.5
        );
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
                "Éviter un mat en un",
                Situations.mateInOneRisk(),
                Actions.avoidMateInOne()
            ),
            rule(
                "Éliminer un défenseur surchargé",
                Situations.removeOverloadedDefenderOpportunity(),
                Actions.removeOverloadedDefender()
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
                "Attaque à la découverte",
                Situations.discoveredAttackOpportunity(),
                Actions.playBestDiscoveredAttack()
            ),
            rule(
                "Créer une fourchette",
                Situations.forkOpportunity(),
                Actions.playBestFork()
            ),
            rule(
                "Donner échec",
                Situations.checkAvailable(),
                Actions.playBestCheck()
            ),
            rule(
                "Prendre une pièce pendue",
                Situations.hangingEnemyPiece(),
                Actions.captureHighestValue()
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
