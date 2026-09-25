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
 * Bot de référence fondé sur une évaluation globale de position.
 *
 * <p>Après les urgences tactiques, il ne cherche pas un motif précis :
 * il simule tous les coups légaux et choisit la position finale la mieux
 * évaluée par le framework.</p>
 */
public final class PositionalBot extends ChessBot {

    @Override
    public BotMetadata metadata() {
        return new BotMetadata(
            "Positional Bot",
            "AstroWare Conception",
            "Bot positionnel : compare tous les coups selon une note globale de position."
        );
    }

    @Override
    protected StrategyProfile strategyProfile() {
        return StrategyProfiles.solid();
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
                "Évaluer la position",
                Situations.always(),
                Actions.bestPosition()
            )
        );
    }
}
