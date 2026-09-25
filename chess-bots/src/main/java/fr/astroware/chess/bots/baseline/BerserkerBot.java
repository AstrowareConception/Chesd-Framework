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
 * Bot offensif de référence.
 *
 * <p>BerserkerBot accepte beaucoup plus facilement les captures défendues et
 * cherche le centre avant de sécuriser son roi. Son intérêt est pédagogique :
 * il utilise les mêmes briques que CautiousBot, mais dans un ordre et avec un
 * profil très différents.</p>
 */
public final class BerserkerBot extends ChessBot {

    @Override
    public BotMetadata metadata() {
        return new BotMetadata(
            "Berserker",
            "AstroWare Conception",
            "Bot offensif : initiative, matériel et prise de risque."
        );
    }

    @Override
    protected StrategyProfile strategyProfile() {
        return StrategyProfiles.adventurous();
    }

    @Override
    protected List<Rule<?>> rules() {
        return List.of(
            rule(
                "Capturer agressivement",
                Situations.captureAvailable(),
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
