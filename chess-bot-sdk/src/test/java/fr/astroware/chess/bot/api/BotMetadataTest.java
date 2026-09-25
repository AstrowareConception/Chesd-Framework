package fr.astroware.chess.bot.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BotMetadataTest {

    @Test
    void storesBotAndAuthorNames() {
        BotMetadata metadata = new BotMetadata(
            "Deep Rabbit",
            "Alice Dupont",
            "Bot tactique"
        );

        assertEquals("Deep Rabbit", metadata.botName());
        assertEquals("Alice Dupont", metadata.authorName());
        assertEquals("Bot tactique", metadata.description());
    }

    @Test
    void rejectsBlankBotName() {
        assertThrows(
            IllegalArgumentException.class,
            () -> BotMetadata.of("   ", "Alice Dupont")
        );
    }

    @Test
    void rejectsBlankAuthorName() {
        assertThrows(
            IllegalArgumentException.class,
            () -> BotMetadata.of("Deep Rabbit", " ")
        );
    }
}
