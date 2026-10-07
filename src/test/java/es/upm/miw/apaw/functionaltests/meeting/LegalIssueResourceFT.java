package es.upm.miw.apaw.functionaltests.meeting;

import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static java.time.temporal.ChronoUnit.MICROS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

@SpringBootTest(classes = LegalIssueResourceFT.ClientConfiguration.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class LegalIssueResourceFT {
    // Identificadores de MeetingSeederForDev, en apaw-practice.
    private static final String ISSUE_PREFIX = "22222222-3333-4444-5555-66667777";
    private static final UUID ISSUE_ID_0 = UUID.fromString(ISSUE_PREFIX + "0000");
    private static final UUID ISSUE_ID_3 = UUID.fromString(ISSUE_PREFIX + "0003");
    private static final UUID UNKNOWN_ID = UUID.fromString(ISSUE_PREFIX + "9999");
    // Identificadores de SeederForDev, en apaw-user: participantes de las reuniones sembradas.
    private static final String USER_PREFIX = "aaaaaaaa-bbbb-cccc-dddd-eeeeffff";
    private static final UUID USER_ID_0 = UUID.fromString(USER_PREFIX + "0000");
    private static final UUID USER_ID_3 = UUID.fromString(USER_PREFIX + "0003");

    @Configuration
    @EnableAutoConfiguration
    @EnableFeignClients(clients = LegalIssueClient.class)
    static class ClientConfiguration {
    }

    @Autowired
    private LegalIssueClient client;

    @Test
    void testCreateReadUpdateAndDelete() {
        LegalIssue created = this.client.create(this.newIssue());
        assertThat(created.getId()).isNotNull();
        assertThat(created.getCreationDate()).isNotNull();
        assertThat(created.getResolved()).isFalse();
        assertThat(created.getPriority()).isEqualTo(5);
        assertThat(created.getDescription()).isEqualTo("Feign description");
        this.assertStored(created);

        LegalIssue replacement = LegalIssue.builder()
                .title("Feign legal issue updated " + UUID.randomUUID()).priority(9).resolved(true).build();
        LegalIssue updated = this.client.update(created.getId(), replacement);
        assertThat(updated.getId()).isEqualTo(created.getId());
        assertThat(updated.getTitle()).isEqualTo(replacement.getTitle());
        assertThat(updated.getDescription()).isNull();
        assertThat(updated.getPriority()).isEqualTo(9);
        assertThat(updated.getResolved()).isTrue();
        assertThat(updated.getCreationDate()).isCloseTo(created.getCreationDate(), within(1, MICROS));

        this.client.delete(created.getId());
        assertThatThrownBy(() -> this.client.read(created.getId())).isInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void testRead() {
        LegalIssue legalIssue = this.client.read(ISSUE_ID_0);
        assertThat(legalIssue.getId()).isEqualTo(ISSUE_ID_0);
        assertThat(legalIssue.getTitle()).isEqualTo("Breach of contract analysis");
        assertThat(legalIssue.getDescription()).isEqualTo("Assess the clauses breached by the supplier");
        assertThat(legalIssue.getPriority()).isEqualTo(1);
        assertThat(legalIssue.getResolved()).isFalse();
        assertThat(legalIssue.getCreationDate()).isEqualTo(LocalDateTime.of(2025, 1, 5, 9, 0));
    }

    @Test
    void testReadWithoutDescription() {
        LegalIssue legalIssue = this.client.read(ISSUE_ID_3);
        assertThat(legalIssue.getTitle()).isEqualTo("Debt acknowledgement draft");
        assertThat(legalIssue.getDescription()).isNull();
    }

    @Test
    void testReadNotFound() {
        assertThatThrownBy(() -> this.client.read(UNKNOWN_ID)).isInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void testFindAllIsSortedByTitle() {
        assertThat(this.client.findAll()).extracting(LegalIssue::getTitle)
                .contains("Breach of contract analysis", "Compensation calculation",
                        "Custody arrangement review", "Debt acknowledgement draft",
                        "Expert witness selection", "Settlement offer assessment")
                .isSorted();
    }

    @Test
    void testFindParticipantReportHydratesParticipants() {
        List<MeetingParticipantReport> reports = this.client.findParticipantReport();
        // cliente0 participa en MEETING_0 (asuntos de prioridad 1 y 2) y en MEETING_2 (prioridad 2).
        assertThat(reports)
                .filteredOn(report -> report.getUserSnapshot().getId().equals(USER_ID_0))
                .singleElement()
                .satisfies(report -> {
                    assertThat(report.getUserSnapshot().getMobile()).isEqualTo("600000100");
                    assertThat(report.getUserSnapshot().getFirstName()).isEqualTo("cliente0");
                    assertThat(report.getMeetingCount()).isEqualTo(2);
                    assertThat(report.getLegalIssueCount()).isEqualTo(3);
                    assertThat(report.getAverageLegalIssuePriority()).isCloseTo(1.67, within(0.01));
                });
        // cliente3 solo participa en MEETING_2, con un único asunto de prioridad 2.
        assertThat(reports)
                .filteredOn(report -> report.getUserSnapshot().getId().equals(USER_ID_3))
                .singleElement()
                .satisfies(report -> {
                    assertThat(report.getUserSnapshot().getFirstName()).isEqualTo("cliente3");
                    assertThat(report.getMeetingCount()).isEqualTo(1);
                    assertThat(report.getLegalIssueCount()).isEqualTo(1);
                    assertThat(report.getAverageLegalIssuePriority()).isCloseTo(2.0, within(0.01));
                });
    }

    @Test
    void testFindParticipantReportIsSortedByHandledLegalIssues() {
        assertThat(this.client.findParticipantReport()).isNotEmpty()
                .extracting(MeetingParticipantReport::getLegalIssueCount)
                .isSortedAccordingTo(Comparator.reverseOrder());
    }

    @Test
    void testCreateDuplicateTitle() {
        LegalIssue legalIssue = this.newIssue();
        legalIssue.setTitle("Breach of contract analysis");
        assertThatThrownBy(() -> this.client.create(legalIssue)).isInstanceOf(FeignException.Conflict.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void testCreateInvalidTitle(String title) {
        LegalIssue legalIssue = this.newIssue();
        legalIssue.setTitle(title);
        assertThatThrownBy(() -> this.client.create(legalIssue)).isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testCreateWithoutPriority() {
        LegalIssue legalIssue = this.newIssue();
        legalIssue.setPriority(null);
        assertThatThrownBy(() -> this.client.create(legalIssue)).isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testUpdateNotFound() {
        LegalIssue legalIssue = this.newIssue();
        assertThatThrownBy(() -> this.client.update(UNKNOWN_ID, legalIssue))
                .isInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void testUpdateDuplicateTitle() {
        LegalIssue created = this.client.create(this.newIssue());
        LegalIssue replacement = LegalIssue.builder().title("Breach of contract analysis").priority(1).build();

        assertThatThrownBy(() -> this.client.update(created.getId(), replacement))
                .isInstanceOf(FeignException.Conflict.class);

        this.assertStored(created);
        this.client.delete(created.getId());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void testUpdateInvalidTitle(String title) {
        LegalIssue replacement = LegalIssue.builder().title(title).priority(1).build();
        assertThatThrownBy(() -> this.client.update(ISSUE_ID_0, replacement))
                .isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testDeleteMissingLegalIssueIsIdempotent() {
        this.client.delete(UNKNOWN_ID);
        assertThatThrownBy(() -> this.client.read(UNKNOWN_ID)).isInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void testDeleteReferencedLegalIssue() {
        assertThatThrownBy(() -> this.client.delete(ISSUE_ID_0)).isInstanceOf(FeignException.Conflict.class);
        assertThat(this.client.read(ISSUE_ID_0).getTitle()).isEqualTo("Breach of contract analysis");
    }

    private void assertStored(LegalIssue expected) {
        LegalIssue stored = this.client.read(expected.getId());
        assertThat(stored).usingRecursiveComparison().ignoringFields("creationDate").isEqualTo(expected);
        assertThat(stored.getCreationDate()).isCloseTo(expected.getCreationDate(), within(1, MICROS));
    }

    private LegalIssue newIssue() {
        return LegalIssue.builder()
                .title("Feign legal issue " + UUID.randomUUID())
                .description("Feign description")
                .priority(5)
                .build();
    }
}
