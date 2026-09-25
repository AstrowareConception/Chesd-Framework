package fr.astroware.chess.bots.baseline;

import fr.astroware.chess.bot.action.Actions;
import fr.astroware.chess.bot.api.BotMetadata;
import fr.astroware.chess.bot.api.ChessBot;
import fr.astroware.chess.bot.rule.Rule;
import fr.astroware.chess.bot.search.SearchSettings;
import fr.astroware.chess.bot.situation.Situations;
import fr.astroware.chess.bot.strategy.StrategyProfile;
import fr.astroware.chess.bot.strategy.StrategyProfiles;

import java.util.List;

/**
 * Bot de référence utilisant une recherche Minimax bornée.
 *
 * <p>Configuration choisie pour rester démontrable en cours :
 * profondeur 3, six coups maximum explorés par nœud, alpha-bêta activé.</p>
 */
public final class MinimaxBot extends ChessBot {

    private static final SearchSettings SEARCH =
        SearchSettings.bounded(3, 6);

    @Override
    public BotMetadata metadata() {
        return new BotMetadata(
            "Minimax Bot",
            "AstroWare Conception",
            "Bot de recherche : Minimax profondeur 3 avec alpha-bêta."
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
                "Recherche Minimax",
                Situations.always(),
                Actions.minimax(SEARCH)
            )
        );
    }
}
