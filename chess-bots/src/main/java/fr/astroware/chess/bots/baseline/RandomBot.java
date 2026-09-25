package fr.astroware.chess.bots.baseline;

import fr.astroware.chess.bot.action.Actions;
import fr.astroware.chess.bot.api.BotMetadata;
import fr.astroware.chess.bot.api.ChessBot;
import fr.astroware.chess.bot.rule.Rule;
import fr.astroware.chess.bot.situation.Situations;

import java.util.List;

/**
 * Bot de référence le plus simple du framework.
 *
 * <p>Il ne possède aucune stratégie : à chaque tour, la situation
 * {@code always()} est reconnue puis l'action {@code randomLegalMove()}
 * choisit un coup légal au hasard.</p>
 *
 * <p>Ce bot constitue le premier exemple à lire pour un étudiant.</p>
 */
public final class RandomBot extends ChessBot {

    @Override
    public BotMetadata metadata() {
        return new BotMetadata(
            "Random Bot",
            "AstroWare Conception",
            "Bot de référence : joue un coup légal au hasard."
        );
    }

    @Override
    protected List<Rule<?>> rules() {
        return List.of(
            rule(
                "Jouer au hasard",
                Situations.always(),
                Actions.randomLegalMove()
            )
        );
    }
}
