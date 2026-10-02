package es.upm.miw.apaw.functionaltests.courthearing;

import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = CourtHearingResourceFT.ClientConfiguration.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class CourtHearingResourceFT {
    private static final UUID COURT_0_ID = UUID.fromString("cccccccc-dddd-eeee-ffff-aaaaaaaa0000");
    private static final UUID HEARING_0_ID = UUID.fromString("dddddddd-eeee-ffff-aaaa-bbbbbbbb0000");
    private static final UUID HEARING_1_ID = UUID.fromString("dddddddd-eeee-ffff-aaaa-bbbbbbbb0001");
    private static final UUID HEARING_2_ID = UUID.fromString("dddddddd-eeee-ffff-aaaa-bbbbbbbb0002");
    private static final UUID HEARING_3_ID = UUID.fromString("dddddddd-eeee-ffff-aaaa-bbbbbbbb0003");
    private static final UUID HEARING_4_ID = UUID.fromString("dddddddd-eeee-ffff-aaaa-bbbbbbbb0004");
    private static final UUID HEARING_5_ID = UUID.fromString("dddddddd-eeee-ffff-aaaa-bbbbbbbb0005");
    private static final UUID HEARING_6_ID = UUID.fromString("dddddddd-eeee-ffff-aaaa-bbbbbbbb0006");
    private static final UUID USER_0_ID = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0000");
    private static final UUID USER_1_ID = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0001");
    private static final UUID UNKNOWN_ID = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff9999");

    @Configuration
    @EnableAutoConfiguration
    @EnableFeignClients(clients = CourtHearingClient.class)
    static class ClientConfiguration {
    }

    @Autowired
    private CourtHearingClient client;

    private CreationCourtHearing creation() {
        return CreationCourtHearing.builder()
                .date(LocalDateTime.now().plusDays(14).withSecond(0).withNano(0))
                .roomNumber("FT-" + UUID.randomUUID())
                .durationMinutes(60)
                .openToPublic(true)
                .remote(false)
                .type(CourtHearingType.TRIAL)
                .courtId(COURT_0_ID)
                .attendeeIds(List.of(USER_0_ID, USER_1_ID))
                .build();
    }

    @Test
    void testCreate() {
        CreationCourtHearing creation = this.creation();

        CourtHearing actual = this.client.createHearing(creation);

        assertThat(actual.getId()).isNotNull();
        assertThat(actual.getDate()).isEqualTo(creation.getDate());
        assertThat(actual.getRoomNumber()).isEqualTo(creation.getRoomNumber());
        assertThat(actual.getDurationMinutes()).isEqualTo(60);
        assertThat(actual.getOpenToPublic()).isTrue();
        assertThat(actual.getRemote()).isFalse();
        assertThat(actual.getType()).isEqualTo(CourtHearingType.TRIAL);
        assertThat(actual.getStatus()).isEqualTo(CourtHearingStatus.SCHEDULED);
        assertThat(actual.getAttendees()).extracting(attendee -> attendee.getId())
                .containsExactlyInAnyOrderElementsOf(creation.getAttendeeIds());
    }

    @Test
    void testFindAllSeededHearings() {
        assertThat(this.client.findHearings(new CourtHearingFindCriteria()))
                .extracting(hearing -> hearing.getId())
                .contains(HEARING_0_ID, HEARING_1_ID, HEARING_2_ID, HEARING_3_ID,
                        HEARING_4_ID, HEARING_5_ID, HEARING_6_ID);
    }

    @Test
    void testFindScheduledHearings() {
        assertThat(this.client.findHearings(CourtHearingFindCriteria.builder().scheduled(true).build()))
                .extracting(hearing -> hearing.getId())
                .contains(HEARING_1_ID, HEARING_2_ID, HEARING_4_ID)
                .doesNotContain(HEARING_0_ID, HEARING_3_ID, HEARING_5_ID, HEARING_6_ID);
    }

    @Test
    void testFindByDate() {
        assertThat(this.client.findHearings(CourtHearingFindCriteria.builder()
                .date(LocalDate.of(2026, 11, 4)).build()))
                .extracting(hearing -> hearing.getId()).containsExactly(HEARING_1_ID);
    }

    @Test
    void testFindByCourtCity() {
        assertThat(this.client.findHearings(CourtHearingFindCriteria.builder().courtCity("Madrid").build()))
                .extracting(hearing -> hearing.getId())
                .contains(HEARING_0_ID, HEARING_1_ID, HEARING_2_ID, HEARING_5_ID)
                .doesNotContain(HEARING_3_ID, HEARING_4_ID, HEARING_6_ID);
    }

    @Test
    void testFindByAttendeeMobile() {
        assertThat(this.client.findHearings(CourtHearingFindCriteria.builder()
                .userMobile("600000100").build()))
                .extracting(hearing -> hearing.getId())
                .contains(HEARING_0_ID, HEARING_1_ID, HEARING_5_ID);
    }

    @Test
    void testFindByCombinedCriteria() {
        assertThat(this.client.findHearings(CourtHearingFindCriteria.builder()
                .courtCity("Madrid").scheduled(true).build()))
                .extracting(hearing -> hearing.getId())
            .contains(HEARING_1_ID, HEARING_2_ID);
    }

    @Test
    void testFindHearingReport() {
        assertThat(this.client.findHearingByCourtReport())
                .filteredOn(report -> report.getCourtName().equals("Tribunal Supremo"))
                .singleElement().satisfies(report -> {
                    assertThat(report.getTotalHearingCount()).isGreaterThanOrEqualTo(3);
                    assertThat(report.getScheduledHearingCount()).isGreaterThanOrEqualTo(2);
                });
    }

    @Test
    void testCreateWithUnknownCourt() {
        CreationCourtHearing creation = this.creation();
        creation.setCourtId(UNKNOWN_ID);

        assertThatThrownBy(() -> this.client.createHearing(creation))
                .isInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void testCreateWithUnknownAttendee() {
        CreationCourtHearing creation = this.creation();
        creation.setAttendeeIds(List.of(UNKNOWN_ID));

        assertThatThrownBy(() -> this.client.createHearing(creation))
                .isInstanceOf(FeignException.NotFound.class);
    }
}