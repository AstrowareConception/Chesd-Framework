package fr.astroware.chess.bot.api;

import java.util.Objects;

/**
 * Identité publique d'un bot.
 *
 * <p>Ces informations seront utilisées dans les traces, les parties et
 * le classement du tournoi. Le nom du bot et le nom de son auteur font donc
 * partie du contrat du framework, et pas d'un simple commentaire de code.</p>
 *
 * @param botName nom libre choisi pour le bot
 * @param authorName nom de l'étudiant ou de l'équipe
 * @param description courte description du bot
 */
public record BotMetadata(
    String botName,
    String authorName,
    String description
) {

    public BotMetadata {
        botName = requireText(botName, "botName");
        authorName = requireText(authorName, "authorName");
        description = Objects.requireNonNullElse(description, "").trim();
    }

    public static BotMetadata of(String botName, String authorName) {
        return new BotMetadata(botName, authorName, "");
    }

    private static String requireText(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " must not be null");

        String normalized = value.trim();

        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }

        return normalized;
    }
}
