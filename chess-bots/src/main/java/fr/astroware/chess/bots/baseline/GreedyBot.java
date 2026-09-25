package fr.astroware.chess.bots.baseline;

import fr.astroware.chess.bot.action.Actions;
import fr.astroware.chess.bot.api.BotMetadata;
import fr.astroware.chess.bot.api.ChessBot;
import fr.astroware.chess.bot.rule.Rule;
import fr.astroware.chess.bot.situation.Situations;
import fr.astroware.chess.bot.strategy.StrategyProfile;
import fr.astroware.chess.bot.strategy.StrategyProfiles;

import java.util.List;

/**
 * Bot matérialiste de référence.
 *
 * <p>GreedyBot cherche d'abord une capture et privilégie la pièce adverse de
 * plus forte valeur. Il ne calcule pas encore les conséquences à plusieurs
 * coups : il est donc volontairement simple, lisible et parfois imprudent.</p>
 */
public final class GreedyBot extends ChessBot {

    @Override
    public BotMetadata metadata() {
        return new BotMetadata(
            "Greedy Bot",
            "AstroWare Conception",
            "Bot matérialiste : prend la pièce la plus chère qu'il peut capturer."
        );
    }

    @Override
    protected StrategyProfile strategyProfile() {
        return StrategyProfiles.balanced();
    }

    @Override
    protected List<Rule<?>> rules() {
        return List.of(
            rule(
                "Prendre la pièce la plus chère",
                Situations.captureAvailable(),
                Actions.captureHighestValue()
            ),
            rule(
                "Secours",
                Situations.always(),
                Actions.randomLegalMove()
            )
        );
    }
}
