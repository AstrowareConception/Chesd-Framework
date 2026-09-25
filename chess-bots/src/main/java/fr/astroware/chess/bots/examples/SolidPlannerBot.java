package fr.astroware.chess.bots.examples;

import fr.astroware.chess.bot.action.Actions;
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
 * Exemple pédagogique montrant comment assembler une personnalité, des
 * ouvertures et des plans multi-coups.
 *
 * <p>Ce bot n'est pas encore un bot tactique complet. Son rôle principal est
 * de montrer aux étudiants qu'une stratégie peut être exprimée de façon
 * lisible et déclarative.</p>
 */
public final class SolidPlannerBot extends ChessBot {

    @Override
    public BotMetadata metadata() {
        return new BotMetadata(
            "The Architect",
            "AstroWare Conception",
            "Bot d'exemple : ouverture théorique puis jeu positionnel solide."
        );
    }

    @Override
    protected StrategyProfile strategyProfile() {
        return StrategyProfiles.solid();
    }

    @Override
    protected List<Rule<?>> rules() {
        return List.of(
            // Les deux livres coexistent : chacun vérifie la couleur du bot.
            Openings.londonSystem().asRule(),
            Openings.scandinavianDefense().asRule(),

            // Priorité stratégique : mettre le roi à l'abri avant de chercher
            // une occupation plus ambitieuse du centre.
            Plans.castleKingside().asRule(),
            Plans.developMinorPieces().asRule(),
            Plans.takeCenter().asRule(),

            // Dernier recours explicite.
            rule(
                "Secours",
                Situations.always(),
                Actions.randomLegalMove()
            )
        );
    }
}
