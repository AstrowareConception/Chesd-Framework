package fr.astroware.chess.tournament.submission;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class StudentSubmissionValidatorTest {

    @Test
    void repositoryStudentBotsRespectTournamentContract() {
        StudentSubmissionValidator.ValidationReport report =
            new StudentSubmissionValidator()
                .validate();

        assertTrue(report.valid());
    }
}
