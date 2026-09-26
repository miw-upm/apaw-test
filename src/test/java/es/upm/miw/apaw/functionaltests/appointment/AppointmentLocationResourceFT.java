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

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = AppointmentLocationResourceFT.ClientConfiguration.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class AppointmentLocationResourceFT {

    private static final UUID LOCATION_0_ID = UUID.fromString("cccccccc-dddd-eeee-ffff-aaaabbbb0000");
    private static final UUID LOCATION_1_ID = UUID.fromString("cccccccc-dddd-eeee-ffff-aaaabbbb0001");
    private static final UUID LOCATION_2_ID = UUID.fromString("cccccccc-dddd-eeee-ffff-aaaabbbb0002");
    private static final UUID UNKNOWN_ID = UUID.fromString("cccccccc-dddd-eeee-ffff-aaaabbbb9999");

    @Configuration
    @EnableAutoConfiguration
    @EnableFeignClients(clients = AppointmentLocationClient.class)
    static class ClientConfiguration {
    }

    @Autowired
    private AppointmentLocationClient client;

    @Test
    void testCreateUpdatePatchAndDelete() {
        AppointmentLocation location = AppointmentLocation.builder()
                .name("Sala Test " + UUID.randomUUID())
                .city("Madrid")
                .address("Calle Test 1")
                .floor(2)
                .build();

        AppointmentLocation created = this.client.create(location);
        assertThat(created.getId()).isNotNull();
        assertThat(created.getCreationDate()).isNotNull();
        assertThat(created.getName()).isEqualTo(location.getName());
        assertThat(created.getCity()).isEqualTo("Madrid");

        AppointmentLocation updated = this.client.update(created.getId(),
                AppointmentLocation.builder()
                        .name(created.getName())
                        .city("Barcelona")
                        .address("Gran Via 1")
                        .floor(5)
                        .build());
        assertThat(updated.getCity()).isEqualTo("Barcelona");
        assertThat(updated.getFloor()).isEqualTo(5);

        AppointmentLocation patched = this.client.patch(created.getId(),
                AppointmentLocationPatch.builder().room("501").build());
        assertThat(patched.getRoom()).isEqualTo("501");
        assertThat(patched.getCity()).isEqualTo("Barcelona");

        this.client.delete(created.getId());
        assertThatThrownBy(() -> this.client.read(created.getId()))
                .isInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void testRead() {
        AppointmentLocation location = this.client.read(LOCATION_0_ID);
        assertThat(location.getId()).isEqualTo(LOCATION_0_ID);
        assertThat(location.getName()).isEqualTo("Sala de Reuniones A");
        assertThat(location.getCity()).isEqualTo("Madrid");
        assertThat(location.getFloor()).isEqualTo(1);
    }

    @Test
    void testFindAll() {
        assertThat(this.client.findAll())
                .extracting(AppointmentLocation::getId)
                .contains(LOCATION_0_ID, LOCATION_1_ID, LOCATION_2_ID);
    }

    @Test
    void testFindAllAlphabeticalOrder() {
        assertThat(this.client.findAll())
                .extracting(AppointmentLocation::getName)
                .containsSubsequence("Oficina Central", "Sala de Conferencias B", "Sala de Reuniones A");
    }

    @Test
    void testCreateDuplicateName() {
        AppointmentLocation duplicate = AppointmentLocation.builder()
                .name("Sala de Reuniones A")
                .city("Madrid")
                .build();
        assertThatThrownBy(() -> this.client.create(duplicate))
                .isInstanceOf(FeignException.Conflict.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void testCreateInvalidName(String name) {
        AppointmentLocation location = AppointmentLocation.builder()
                .name(name)
                .city("Madrid")
                .build();
        assertThatThrownBy(() -> this.client.create(location))
                .isInstanceOf(FeignException.BadRequest.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void testCreateInvalidCity(String city) {
        AppointmentLocation location = AppointmentLocation.builder()
                .name("Sala " + UUID.randomUUID())
                .city(city)
                .build();
        assertThatThrownBy(() -> this.client.create(location))
                .isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testReadNotFound() {
        assertThatThrownBy(() -> this.client.read(UNKNOWN_ID))
                .isInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void testDeleteNotFound() {
        assertThatThrownBy(() -> this.client.delete(UNKNOWN_ID))
                .isInstanceOf(FeignException.NotFound.class);
    }
}