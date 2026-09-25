package fr.astroware.chess.tournament.submission;

import fr.astroware.chess.bots.students.ValidatorFixtureBot;
import fr.astroware.chess.tournament.cli.BotCatalog;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StudentBotDiscoveryTest {

    @Test
    void discoversCompiledStudentBot() {
        assertTrue(
            StudentBotDiscovery.discover()
                .contains(
                    ValidatorFixtureBot.class
                )
        );
    }

    @Test
    void automaticallyRegistersStudentBotInCatalog() {
        assertTrue(
            BotCatalog.find(
                "student-validator-fixture"
            ).isPresent()
        );

        assertEquals(
            ValidatorFixtureBot.class,
            BotCatalog.findClass(
                "student-validator-fixture"
            ).orElseThrow()
        );
    }
}
