package es.upm.miw.apaw.functionaltests.appointment;

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
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = AppointmentResourceFT.ClientConfiguration.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class AppointmentResourceFT {

    private static final UUID CLIENT_ID = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0000");
    private static final String CLIENT_MOBILE = "600000100";
    private static final UUID LOCATION_0_ID = UUID.fromString("cccccccc-dddd-eeee-ffff-aaaabbbb0000");
    private static final UUID UNKNOWN_ID = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff9999");

    @Configuration
    @EnableAutoConfiguration
    @EnableFeignClients(clients = AppointmentClient.class)
    static class ClientConfiguration {
    }

    @Autowired
    private AppointmentClient client;

    private CreationAppointment creation() {
        return CreationAppointment.builder()
                .title("Appointment " + UUID.randomUUID())
                .scheduledDate(LocalDateTime.now().plusDays(7))
                .userId(CLIENT_ID)
                .build();
    }

    @Test
    void testCreate() {
        CreationAppointment creation = this.creation();
        creation.setLocationId(LOCATION_0_ID);

        Appointment actual = this.client.create(creation);

        assertThat(actual.getId()).isNotNull();
        assertThat(actual.getCreationDate()).isNotNull();
        assertThat(actual.getTitle()).isEqualTo(creation.getTitle());
        assertThat(actual.getDurationMinutes()).isEqualTo(30);
        assertThat(actual.getVirtual()).isFalse();
        assertThat(actual.getStatus()).isEqualTo(AppointmentStatus.SCHEDULED);
        assertThat(actual.getClient().getId()).isEqualTo(CLIENT_ID);
        assertThat(actual.getClient().getMobile()).isEqualTo(CLIENT_MOBILE);
        assertThat(actual.getLocation().getId()).isEqualTo(LOCATION_0_ID);
    }

    @Test
    void testCreateWithoutLocation() {
        Appointment actual = this.client.create(this.creation());

        assertThat(actual.getId()).isNotNull();
        assertThat(actual.getStatus()).isEqualTo(AppointmentStatus.SCHEDULED);
        assertThat(actual.getLocation()).isNull();
        assertThat(actual.getClient().getId()).isEqualTo(CLIENT_ID);
    }

    @Test
    void testFindAll() {
        Appointment created = this.client.create(this.creation());

        List<Appointment> result = this.client.find(new AppointmentFindCriteria());

        assertThat(result).extracting(Appointment::getId).contains(created.getId());
    }

    @Test
    void testFindByStatus() {
        Appointment created = this.client.create(this.creation());

        assertThat(this.client.find(AppointmentFindCriteria.builder()
                .status(AppointmentStatus.SCHEDULED).build()))
                .extracting(Appointment::getId).contains(created.getId());
        assertThat(this.client.find(AppointmentFindCriteria.builder()
                .status(AppointmentStatus.CANCELLED).build()))
                .extracting(Appointment::getId).doesNotContain(created.getId());
    }

    @Test
    void testFindUpcoming() {
        CreationAppointment creation = this.creation();
        creation.setScheduledDate(LocalDateTime.now().plusDays(30));
        Appointment future = this.client.create(creation);

        assertThat(this.client.find(AppointmentFindCriteria.builder().upcoming(true).build()))
                .extracting(Appointment::getId).contains(future.getId());
    }

    @Test
    void testFindByCity() {
        CreationAppointment creation = this.creation();
        creation.setLocationId(LOCATION_0_ID);
        Appointment withMadrid = this.client.create(creation);

        assertThat(this.client.find(AppointmentFindCriteria.builder().city("Madrid").build()))
                .extracting(Appointment::getId).contains(withMadrid.getId());
        assertThat(this.client.find(AppointmentFindCriteria.builder().city("Zaragoza").build()))
                .extracting(Appointment::getId).doesNotContain(withMadrid.getId());
    }

    @Test
    void testFindByClientMobile() {
        Appointment created = this.client.create(this.creation());

        assertThat(this.client.find(AppointmentFindCriteria.builder()
                .clientMobile(CLIENT_MOBILE).build()))
                .extracting(Appointment::getId).contains(created.getId());
        assertThat(this.client.find(AppointmentFindCriteria.builder()
                .clientMobile("699999999").build()))
                .extracting(Appointment::getId).doesNotContain(created.getId());
    }

    @Test
    void testFindCityReport() {
        CreationAppointment creation = this.creation();
        creation.setLocationId(LOCATION_0_ID);
        this.client.create(creation);

        List<AppointmentCityReport> report = this.client.findCityReport();

        assertThat(report).isNotEmpty();
        assertThat(report).allSatisfy(r -> {
            assertThat(r.getCity()).isNotBlank();
            assertThat(r.getTotalAppointments()).isPositive();
            assertThat(r.getClient()).isNotNull();
        });
        assertThat(report).extracting(AppointmentCityReport::getTotalAppointments)
                .isSortedAccordingTo((a, b) -> Long.compare(b, a));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void testCreateInvalidTitle(String title) {
        CreationAppointment creation = this.creation();
        creation.setTitle(title);
        assertThatThrownBy(() -> this.client.create(creation))
                .isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testCreateWithoutScheduledDate() {
        CreationAppointment creation = this.creation();
        creation.setScheduledDate(null);
        assertThatThrownBy(() -> this.client.create(creation))
                .isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testCreateWithoutUserId() {
        CreationAppointment creation = this.creation();
        creation.setUserId(null);
        assertThatThrownBy(() -> this.client.create(creation))
                .isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testCreateUnknownLocation() {
        CreationAppointment creation = this.creation();
        creation.setLocationId(UNKNOWN_ID);
        assertThatThrownBy(() -> this.client.create(creation))
                .isInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void testCreateUnknownUser() {
        CreationAppointment creation = this.creation();
        creation.setUserId(UNKNOWN_ID);
        assertThatThrownBy(() -> this.client.create(creation))
                .isInstanceOf(FeignException.NotFound.class);
    }
}