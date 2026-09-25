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
 * Bot prudent de référence.
 *
 * <p>Il privilégie d'abord les prises gratuites, puis les captures évaluées
 * avec une pénalité de risque. En l'absence de tactique immédiate, il cherche
 * à sécuriser son roi et à développer ses pièces.</p>
 */
public final class CautiousBot extends ChessBot {

    @Override
    public BotMetadata metadata() {
        return new BotMetadata(
            "Cautious Bot",
            "AstroWare Conception",
            "Bot prudent : évite autant que possible les échanges risqués."
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
                "Prendre une pièce pendue",
                Situations.hangingEnemyPiece(),
                Actions.captureHighestValue()
            ),
            rule(
                "Évaluer une capture prudente",
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
