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
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static java.time.temporal.ChronoUnit.MINUTES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = MeetingResourceFT.ClientConfiguration.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class MeetingResourceFT {
    // Identificadores de MeetingSeederForDev, en apaw-practice.
    private static final String MEETING_PREFIX = "33333333-4444-5555-6666-77778888";
    private static final UUID MEETING_ID_0 = UUID.fromString(MEETING_PREFIX + "0000");
    private static final UUID MEETING_ID_1 = UUID.fromString(MEETING_PREFIX + "0001");
    private static final UUID MEETING_ID_2 = UUID.fromString(MEETING_PREFIX + "0002");
    private static final String ISSUE_PREFIX = "22222222-3333-4444-5555-66667777";
    private static final UUID ASSIGNED_ISSUE_ID = UUID.fromString(ISSUE_PREFIX + "0000");
    // ISSUE_5 no pertenece a ninguna reunión: solo se usa en peticiones que deben fallar.
    private static final UUID FREE_ISSUE_ID = UUID.fromString(ISSUE_PREFIX + "0005");
    private static final UUID UNKNOWN_ISSUE_ID = UUID.fromString(ISSUE_PREFIX + "9999");
    // Identificadores de SeederForDev, en apaw-user.
    private static final String USER_PREFIX = "aaaaaaaa-bbbb-cccc-dddd-eeeeffff";
    private static final UUID USER_ID_2 = UUID.fromString(USER_PREFIX + "0002");
    // cliente4 y cliente5 no participan en ninguna reunión sembrada: las reuniones creadas
    // por estas pruebas no alteran el informe de participantes verificado en LegalIssueResourceFT.
    private static final UUID USER_ID_4 = UUID.fromString(USER_PREFIX + "0004");
    private static final UUID USER_ID_5 = UUID.fromString(USER_PREFIX + "0005");
    private static final UUID UNKNOWN_USER_ID = UUID.fromString(USER_PREFIX + "9999");

    @Configuration
    @EnableAutoConfiguration
    @EnableFeignClients(clients = {MeetingClient.class, LegalIssueClient.class})
    static class ClientConfiguration {
    }

    @Autowired
    private MeetingClient client;
    @Autowired
    private LegalIssueClient legalIssueClient;

    @Test
    void testCreate() {
        UUID legalIssueId = this.createLegalIssueId();
        CreationMeeting creation = this.creation(List.of(legalIssueId), List.of(USER_ID_4, USER_ID_5));

        Meeting meeting = this.client.create(creation);

        assertThat(meeting.getId()).isNotNull();
        assertThat(meeting.getTitle()).isEqualTo(creation.getTitle());
        assertThat(meeting.getMeetingDate()).isEqualTo(creation.getMeetingDate());
        assertThat(meeting.getLocation()).isEqualTo("Feign meeting room");
        assertThat(meeting.getDurationMinutes()).isEqualTo(60);
        assertThat(meeting.getDescription()).isEqualTo("Feign description");
        assertThat(meeting.getOnline()).isFalse();
        assertThat(meeting.getMeetingStatus()).isEqualTo(MeetingStatus.SCHEDULED);
        assertThat(meeting.getLegalIssues()).extracting(LegalIssue::getId).containsExactly(legalIssueId);
        assertThat(meeting.getParticipants()).extracting(UserSnapshot::getId)
                .containsExactly(USER_ID_4, USER_ID_5);
        assertThat(meeting.getParticipants()).extracting(UserSnapshot::getMobile)
                .containsExactly("600000104", "600000105");
        assertThat(meeting.getParticipants()).extracting(UserSnapshot::getFirstName)
                .containsExactly("cliente4", "cliente5");
        // La reunión es futura y está planificada, luego sigue abierta.
        assertThat(this.client.find(MeetingFindCriteria.builder().opened(true).build()))
                .extracting(Meeting::getId).contains(meeting.getId());
        assertThat(this.client.find(MeetingFindCriteria.builder().participantFirstName("cliente4").build()))
                .extracting(Meeting::getId).contains(meeting.getId());
    }

    @Test
    void testCreateKeepsProvidedOnline() {
        CreationMeeting creation = this.creation(
                List.of(this.createLegalIssueId()), List.of(USER_ID_4));
        creation.setOnline(true);

        assertThat(this.client.create(creation).getOnline()).isTrue();
    }

    @Test
    void testCreateDeduplicatesParticipants() {
        CreationMeeting creation = this.creation(
                List.of(this.createLegalIssueId()), List.of(USER_ID_4, USER_ID_4));

        assertThat(this.client.create(creation).getParticipants())
                .extracting(UserSnapshot::getId).containsExactly(USER_ID_4);
    }

    @Test
    void testCreateWithoutParticipants() {
        CreationMeeting creation = this.creation(List.of(this.createLegalIssueId()), List.of());

        assertThat(this.client.create(creation).getParticipants()).isEmpty();
    }

    @Test
    void testCreateDuplicateTitle() {
        CreationMeeting creation = this.creation(List.of(FREE_ISSUE_ID), List.of(USER_ID_4));
        creation.setTitle("Initial case review");

        assertThatThrownBy(() -> this.client.create(creation)).isInstanceOf(FeignException.Conflict.class);
    }

    @Test
    void testCreateLegalIssueNotFound() {
        CreationMeeting creation = this.creation(List.of(UNKNOWN_ISSUE_ID), List.of(USER_ID_4));

        assertThatThrownBy(() -> this.client.create(creation)).isInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void testCreateLegalIssueAlreadyAssignedToAnotherMeeting() {
        CreationMeeting creation = this.creation(List.of(ASSIGNED_ISSUE_ID), List.of(USER_ID_4));

        assertThatThrownBy(() -> this.client.create(creation)).isInstanceOf(FeignException.Conflict.class);
    }

    @Test
    void testCreateUserNotFound() {
        CreationMeeting creation = this.creation(
                List.of(FREE_ISSUE_ID), List.of(USER_ID_4, UNKNOWN_USER_ID));

        assertThatThrownBy(() -> this.client.create(creation)).isInstanceOf(FeignException.NotFound.class);
        // La reunión no se ha creado, luego el asunto legal sigue libre.
        assertThat(this.legalIssueClient.read(FREE_ISSUE_ID).getTitle())
                .isEqualTo("Settlement offer assessment");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void testCreateInvalidTitle(String title) {
        CreationMeeting creation = this.creation(List.of(FREE_ISSUE_ID), List.of(USER_ID_4));
        creation.setTitle(title);

        assertThatThrownBy(() -> this.client.create(creation)).isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testCreateWithoutMeetingDate() {
        CreationMeeting creation = this.creation(List.of(FREE_ISSUE_ID), List.of(USER_ID_4));
        creation.setMeetingDate(null);

        assertThatThrownBy(() -> this.client.create(creation)).isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testCreateWithoutDurationMinutes() {
        CreationMeeting creation = this.creation(List.of(FREE_ISSUE_ID), List.of(USER_ID_4));
        creation.setDurationMinutes(null);

        assertThatThrownBy(() -> this.client.create(creation)).isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testCreateWithoutLegalIssues() {
        CreationMeeting creation = this.creation(List.of(FREE_ISSUE_ID), List.of(USER_ID_4));
        creation.setLegalIssueIds(null);

        assertThatThrownBy(() -> this.client.create(creation)).isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testCreateEmptyLegalIssues() {
        CreationMeeting creation = this.creation(List.of(), List.of(USER_ID_4));

        assertThatThrownBy(() -> this.client.create(creation)).isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testCreateNullLegalIssueId() {
        CreationMeeting creation = this.creation(Arrays.asList((UUID) null), List.of(USER_ID_4));

        assertThatThrownBy(() -> this.client.create(creation)).isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testCreateWithoutParticipantIds() {
        CreationMeeting creation = this.creation(List.of(FREE_ISSUE_ID), List.of());
        creation.setParticipantIds(null);

        assertThatThrownBy(() -> this.client.create(creation)).isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testFindAllIsSortedByMeetingDate() {
        assertThat(this.client.find(new MeetingFindCriteria())).extracting(Meeting::getId)
                .contains(MEETING_ID_0, MEETING_ID_1, MEETING_ID_2)
                .containsSubsequence(MEETING_ID_0, MEETING_ID_1, MEETING_ID_2);
    }

    @Test
    void testFindDoesNotLoadLegalIssues() {
        assertThat(this.client.find(new MeetingFindCriteria())).isNotEmpty()
                .allSatisfy(meeting -> assertThat(meeting.getLegalIssues()).isNull());
    }

    @Test
    void testFindHydratesParticipants() {
        assertThat(this.client.find(new MeetingFindCriteria()))
                .filteredOn(meeting -> meeting.getId().equals(MEETING_ID_1))
                .singleElement()
                .satisfies(meeting -> assertThat(meeting.getParticipants()).singleElement()
                        .satisfies(participant -> {
                            assertThat(participant.getId()).isEqualTo(USER_ID_2);
                            assertThat(participant.getMobile()).isEqualTo("600000102");
                            assertThat(participant.getFirstName()).isEqualTo("cliente2");
                        }));
    }

    @Test
    void testFindByMinDurationMinutes() {
        assertThat(this.client.find(MeetingFindCriteria.builder().minDurationMinutes(60).build()))
                .extracting(Meeting::getId)
                .contains(MEETING_ID_0, MEETING_ID_1)
                .doesNotContain(MEETING_ID_2);
    }

    @Test
    void testFindByOpened() {
        // Las tres reuniones sembradas son de 2025: ninguna sigue abierta.
        assertThat(this.client.find(MeetingFindCriteria.builder().opened(false).build()))
                .extracting(Meeting::getId).contains(MEETING_ID_0, MEETING_ID_1, MEETING_ID_2);
        assertThat(this.client.find(MeetingFindCriteria.builder().opened(true).build()))
                .extracting(Meeting::getId).doesNotContain(MEETING_ID_0, MEETING_ID_1, MEETING_ID_2);
    }

    @Test
    void testFindByMaxLegalIssuePriority() {
        assertThat(this.client.find(MeetingFindCriteria.builder().maxLegalIssuePriority(1).build()))
                .extracting(Meeting::getId)
                .contains(MEETING_ID_0, MEETING_ID_1)
                .doesNotContain(MEETING_ID_2);
    }

    @Test
    void testFindByParticipantFirstName() {
        assertThat(this.client.find(MeetingFindCriteria.builder().participantFirstName("cliente0").build()))
                .extracting(Meeting::getId)
                .contains(MEETING_ID_0, MEETING_ID_2)
                .doesNotContain(MEETING_ID_1);
    }

    @Test
    void testFindByUnknownParticipantFirstName() {
        assertThat(this.client.find(MeetingFindCriteria.builder().participantFirstName("desconocido").build()))
                .isEmpty();
    }

    @Test
    void testFindCombinesEveryCriteria() {
        assertThat(this.client.find(MeetingFindCriteria.builder()
                .minDurationMinutes(60)
                .opened(false)
                .maxLegalIssuePriority(1)
                .participantFirstName("cliente0")
                .build()))
                .extracting(Meeting::getId)
                .contains(MEETING_ID_0)
                .doesNotContain(MEETING_ID_1, MEETING_ID_2);
    }

    // No hay DELETE de reuniones: cada ejecución usa un título y un asunto legal nuevos.
    private CreationMeeting creation(List<UUID> legalIssueIds, List<UUID> participantIds) {
        return CreationMeeting.builder()
                .title("Feign meeting " + UUID.randomUUID())
                .meetingDate(LocalDateTime.now().plusDays(10).truncatedTo(MINUTES))
                .location("Feign meeting room")
                .durationMinutes(60)
                .description("Feign description")
                .legalIssueIds(legalIssueIds)
                .participantIds(participantIds)
                .build();
    }

    private UUID createLegalIssueId() {
        return this.legalIssueClient.create(LegalIssue.builder()
                .title("Feign meeting legal issue " + UUID.randomUUID())
                .priority(1)
                .build()).getId();
    }
}
