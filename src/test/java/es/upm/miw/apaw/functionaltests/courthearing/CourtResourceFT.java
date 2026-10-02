package es.upm.miw.apaw.functionaltests.courthearing;

import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = CourtResourceFT.ClientConfiguration.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class CourtResourceFT {
    private static final UUID COURT_0_ID = UUID.fromString("cccccccc-dddd-eeee-ffff-aaaaaaaa0000");
    private static final UUID COURT_1_ID = UUID.fromString("cccccccc-dddd-eeee-ffff-aaaaaaaa0001");
    private static final UUID COURT_2_ID = UUID.fromString("cccccccc-dddd-eeee-ffff-aaaaaaaa0002");
    private static final UUID COURT_3_ID = UUID.fromString("cccccccc-dddd-eeee-ffff-aaaaaaaa0003");
    private static final UUID COURT_4_ID = UUID.fromString("cccccccc-dddd-eeee-ffff-aaaaaaaa0004");
    private static final UUID UNKNOWN_ID = UUID.fromString("cccccccc-dddd-eeee-ffff-aaaaaaaa9999");

    @Configuration
    @EnableAutoConfiguration
    @EnableFeignClients(clients = CourtHearingClient.class)
    static class ClientConfiguration {
    }

    @Autowired
    private CourtHearingClient client;

    @Test
    void testCreateUpdatePatchAndDelete() {
        Court court = Court.builder()
                .name("Functional Court " + UUID.randomUUID())
                .address("Test Street 1")
                .city("Madrid")
                .phone("910000000")
                .openingTime(LocalTime.of(9, 0))
                .closingTime(LocalTime.of(17, 0))
                .type(CourtType.CIVIL)
                .build();

        Court created = this.client.createCourt(court);
        assertThat(created.getId()).isNotNull();
        assertThat(created.getName()).isEqualTo(court.getName());
        assertThat(created.getType()).isEqualTo(CourtType.CIVIL);

        Court updated = this.client.updateCourt(created.getId(), Court.builder()
                .name(created.getName())
                .address("Updated Street 2")
                .city("Barcelona")
                .phone("930000000")
                .openingTime(LocalTime.of(8, 30))
                .closingTime(LocalTime.of(16, 0))
                .type(CourtType.APPEAL)
                .build());
        assertThat(updated.getAddress()).isEqualTo("Updated Street 2");
        assertThat(updated.getCity()).isEqualTo("Barcelona");
        assertThat(updated.getType()).isEqualTo(CourtType.APPEAL);

        Court patched = this.client.patchCourt(created.getId(),
                CourtUpdate.builder().phone("940000000").build());
        assertThat(patched.getPhone()).isEqualTo("940000000");
        assertThat(patched.getCity()).isEqualTo("Barcelona");

        this.client.deleteCourt(created.getId());
        assertThatThrownBy(() -> this.client.readCourt(created.getId()))
                .isInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void testReadSeededCourt() {
        Court court = this.client.readCourt(COURT_0_ID);

        assertThat(court.getId()).isEqualTo(COURT_0_ID);
        assertThat(court.getName()).isEqualTo("Tribunal Supremo");
        assertThat(court.getCity()).isEqualTo("Madrid");
        assertThat(court.getType()).isEqualTo(CourtType.SUPREME);
        assertThat(court.getOpeningTime()).isEqualTo(LocalTime.of(9, 0));
    }

    @Test
    void testFindAllSeededCourts() {
        assertThat(this.client.findAllCourts()).extracting(court -> court.getId())
                .contains(COURT_0_ID, COURT_1_ID, COURT_2_ID, COURT_3_ID, COURT_4_ID);
    }

    @Test
    void testFindHearingByCourtReport() {
        assertThat(this.client.findHearingByCourtReport())
                .filteredOn(report -> report.getCourtName().equals("Tribunal Supremo"))
                .singleElement().satisfies(report -> {
                                        assertThat(report.getTotalHearingCount()).isGreaterThanOrEqualTo(3);
                                        assertThat(report.getScheduledHearingCount()).isGreaterThanOrEqualTo(2);
                });
    }

    @Test
    void testReadUnknownCourt() {
        assertThatThrownBy(() -> this.client.readCourt(UNKNOWN_ID))
                .isInstanceOf(FeignException.NotFound.class);
    }

    @Test
        void testDeleteUnknownCourtIsIdempotent() {
                assertThatCode(() -> this.client.deleteCourt(UNKNOWN_ID)).doesNotThrowAnyException();
    }
}