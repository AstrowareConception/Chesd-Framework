package fr.astroware.chess.bots.students;

import fr.astroware.chess.bot.action.Actions;
import fr.astroware.chess.bot.api.BotMetadata;
import fr.astroware.chess.bot.api.ChessBot;
import fr.astroware.chess.bot.rule.Rule;
import fr.astroware.chess.bot.situation.Situations;

import java.util.List;

/**
 * Bot de fixture utilisé uniquement par les tests de workflow étudiant.
 */
public final class ValidatorFixtureBot extends ChessBot {

    public ValidatorFixtureBot() {
    }

    @Override
    public BotMetadata metadata() {
        return new BotMetadata(
            "Validator Fixture",
            "Chess Framework Test Suite",
            "Bot étudiant factice servant à valider la découverte automatique."
        );
    }

    @Override
    protected List<Rule<?>> rules() {
        return List.of(
            rule(
                "Coup légal de secours",
                Situations.always(),
                Actions.randomLegalMove()
            )
        );
    }
}
