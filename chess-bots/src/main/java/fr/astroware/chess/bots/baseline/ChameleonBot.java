package fr.astroware.chess.bots.baseline;

import fr.astroware.chess.bot.action.Actions;
import fr.astroware.chess.bot.analysis.GamePhase;
import fr.astroware.chess.bot.api.BotContext;
import fr.astroware.chess.bot.api.BotMetadata;
import fr.astroware.chess.bot.api.ChessBot;
import fr.astroware.chess.bot.opening.Openings;
import fr.astroware.chess.bot.plan.Plans;
import fr.astroware.chess.bot.rule.Rule;
import fr.astroware.chess.bot.situation.Situations;
import fr.astroware.chess.bot.strategy.StrategyProfile;
import fr.astroware.chess.bot.strategy.StrategyProfiles;

import java.util.List;

/**
 * Bot adaptatif dont le tempérament dépend de la phase de jeu.
 */
public final class ChameleonBot extends ChessBot {

    @Override
    public BotMetadata metadata() {
        return new BotMetadata(
            "Chameleon",
            "AstroWare Conception",
            "Bot adaptatif : solide à l'ouverture, agressif au milieu, prudent en finale."
        );
    }

    @Override
    protected StrategyProfile strategyProfile(BotContext context) {
        return switch (context.analysis().gamePhase()) {
            case OPENING -> StrategyProfiles.solid();
            case MIDDLEGAME -> StrategyProfiles.aggressive();
            case ENDGAME -> StrategyProfiles.defensive();
        };
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

            Openings.londonSystem().asRule(),
            Openings.scandinavianDefense().asRule(),

            rule(
                "Pression de milieu de jeu",
                Situations.onlyInPhase(
                    GamePhase.MIDDLEGAME,
                    Situations.removeOverloadedDefenderOpportunity()
                ),
                Actions.removeOverloadedDefender()
            ),
            rule(
                "Fourchette de milieu de jeu",
                Situations.onlyInPhase(
                    GamePhase.MIDDLEGAME,
                    Situations.forkOpportunity()
                ),
                Actions.playBestFork()
            ),
            rule(
                "Prise sûre",
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
