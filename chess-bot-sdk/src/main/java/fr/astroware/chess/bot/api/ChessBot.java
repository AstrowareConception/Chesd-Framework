package fr.astroware.chess.bot.api;

import fr.astroware.chess.bot.rule.AttemptStatus;
import fr.astroware.chess.bot.rule.Rule;
import fr.astroware.chess.bot.rule.RuleAttempt;
import fr.astroware.chess.core.model.Move;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Classe de base de tous les bots du framework.
 *
 * <p>Le framework contrôle l'algorithme général de décision : les règles sont
 * testées dans l'ordre et la première qui produit un coup légal gagne. C'est
 * une application du pattern Template Method combiné à une Chain of
 * Responsibility.</p>
 *
 * <p>Un étudiant étend cette classe, fournit l'identité de son bot puis
 * construit sa liste de règles. La méthode {@link #decide(BotContext)} est
 * finale afin que tous les bots simples respectent le même protocole.</p>
 */
public abstract class ChessBot {

    /**
     * Identité publique du bot : nom choisi, auteur et description.
     */
    public abstract BotMetadata metadata();

    /**
     * Règles évaluées dans l'ordre.
     */
    protected abstract List<Rule<?>> rules();

    /**
     * Exécute le cycle standard de décision.
     *
     * @throws IllegalStateException si le moteur demande au bot de jouer alors
     *                               qu'aucun coup légal n'existe
     */
    public final BotDecision decide(BotContext context) {
        Objects.requireNonNull(context, "context must not be null");

        List<Move> legalMoves = List.copyOf(context.legalMoves());

        if (legalMoves.isEmpty()) {
            throw new IllegalStateException(
                "ChessBot cannot decide when there is no legal move"
            );
        }

        List<RuleAttempt> trace = new ArrayList<>();

        for (Rule<?> rule : List.copyOf(rules())) {
            RuleAttempt attempt = rule.evaluate(context);
            trace.add(attempt);

            if (attempt.status() == AttemptStatus.SELECTED) {
                return new BotDecision(
                    attempt.proposedMove().orElseThrow(),
                    trace
                );
            }
        }

        /*
         * Filet de sécurité du framework.
         *
         * Un bot étudiant ne doit jamais perdre une partie uniquement parce
         * qu'il a oublié une règle de secours : si aucune règle ne sait jouer,
         * le framework choisit un coup légal aléatoire.
         */
        Move fallback = legalMoves.get(context.random().nextInt(legalMoves.size()));

        trace.add(new RuleAttempt(
            "Framework fallback",
            AttemptStatus.SELECTED,
            0,
            Optional.of(fallback),
            "Aucune règle du bot n'a produit de coup légal"
        ));

        return new BotDecision(fallback, trace);
    }

    /**
     * Raccourci destiné aux classes étudiantes pour garder la déclaration des
     * comportements très lisible.
     */
    protected final <D extends fr.astroware.chess.bot.rule.Detection> Rule<D> rule(
        String name,
        fr.astroware.chess.bot.rule.Situation<D> situation,
        fr.astroware.chess.bot.rule.Action<D> action
    ) {
        return Rule.of(name, situation, action);
    }
}
