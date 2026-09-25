package fr.astroware.chess.tournament.submission;

import fr.astroware.chess.bots.students.ValidatorFixtureBot;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StudentSubmissionValidatorTest {

    @Test
    void repositoryStudentBotsRespectTournamentContract() {
        StudentSubmissionValidator.ValidationReport report =
            new StudentSubmissionValidator()
                .validate();

        assertTrue(report.valid());

        assertTrue(
            report.validatedBots().stream()
                .anyMatch(bot ->
                    bot.className().equals(
                        ValidatorFixtureBot.class.getName()
                    )
                )
        );

        var fixture = report.validatedBots().stream()
            .filter(bot ->
                bot.className().equals(
                    ValidatorFixtureBot.class.getName()
                )
            )
            .findFirst()
            .orElseThrow();

        assertEquals(
            "Validator Fixture",
            fixture.metadata().botName()
        );
    }
}
