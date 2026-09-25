package fr.astroware.chess.tournament.roundrobin;

import fr.astroware.chess.bot.api.BotMetadata;
import fr.astroware.chess.bot.api.ChessBot;
import fr.astroware.chess.tournament.execution.BotPlayer;
import fr.astroware.chess.tournament.execution.BotPlayerFactory;
import fr.astroware.chess.tournament.execution.BotPlayers;
import fr.astroware.chess.tournament.execution.IsolatedBotPlayerFactory;
import fr.astroware.chess.tournament.execution.IsolatedBotSettings;
import fr.astroware.chess.tournament.match.BotFactory;

import java.util.Objects;

/**
 * Participant déclaré dans un tournoi.
 *
 * @param key identifiant court utilisé par le CLI
 * @param playerFactory fabrique le joueur utilisé pour chaque partie
 * @param metadata identité affichée dans le classement
 */
public record TournamentParticipant(
    String key,
    BotPlayerFactory<? extends BotPlayer> playerFactory,
    BotMetadata metadata
) {

    public TournamentParticipant {
        Objects.requireNonNull(
            key,
            "key must not be null"
        );
        Objects.requireNonNull(
            playerFactory,
            "playerFactory must not be null"
        );
        Objects.requireNonNull(
            metadata,
            "metadata must not be null"
        );

        key = key.trim();

        if (key.isEmpty()) {
            throw new IllegalArgumentException(
                "key must not be blank"
            );
        }
    }

    /**
     * Compatibilité avec l'API historique en mémoire.
     */
    public TournamentParticipant(
        String key,
        BotFactory factory
    ) {
        this(
            key,
            BotPlayers.inProcess(factory),
            factory.create().metadata()
        );
    }

    /**
     * Participant exécuté dans une JVM isolée.
     */
    public static TournamentParticipant isolated(
        String key,
        Class<? extends ChessBot> botClass,
        BotMetadata metadata,
        IsolatedBotSettings settings
    ) {
        return new TournamentParticipant(
            key,
            new IsolatedBotPlayerFactory(
                botClass,
                settings
            ),
            metadata
        );
    }
}
