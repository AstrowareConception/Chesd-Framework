package fr.astroware.chess.bots.baseline;

import fr.astroware.chess.bot.action.Actions;
import fr.astroware.chess.bot.analysis.GamePhase;
import fr.astroware.chess.bot.api.BotMetadata;
import fr.astroware.chess.bot.api.ChessBot;
import fr.astroware.chess.bot.plan.Plans;
import fr.astroware.chess.bot.rule.Rule;
import fr.astroware.chess.bot.situation.Situations;
import fr.astroware.chess.bot.strategy.StrategyProfile;
import fr.astroware.chess.bot.strategy.StrategyProfiles;

import java.util.List;

/**
 * Bot positionnel à profondeur 2.
 *
 * <p>Il partage les mêmes heuristiques que PositionalBot, mais son dernier
 * niveau de décision examine la meilleure réponse adverse avant de noter le
 * coup. Huit candidats immédiats sont conservés pour limiter le coût.</p>
 */
public final class LookaheadBot extends ChessBot {

    @Override
    public BotMetadata metadata() {
        return new BotMetadata(
            "Lookahead Bot",
            "AstroWare Conception",
            "Bot positionnel profondeur 2 : anticipe la meilleure réponse adverse."
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
            Plans.improveKingSafety().asRule(
                Situations.inPhase(GamePhase.OPENING)
            ),
            Plans.useOpenFile().asRule(
                Situations.inPhase(GamePhase.MIDDLEGAME)
            ),
            Plans.createPassedPawn().asRule(
                Situations.inPhase(GamePhase.ENDGAME)
            ),
            rule(
                "Anticiper la meilleure réponse",
                Situations.always(),
                Actions.bestPositionAfterBestReply(8)
            )
        );
    }
}
