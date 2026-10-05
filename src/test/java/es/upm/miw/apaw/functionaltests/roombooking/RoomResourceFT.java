package es.upm.miw.apaw.functionaltests.roombooking;

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

import static java.time.temporal.ChronoUnit.MICROS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

@SpringBootTest(classes = RoomResourceFT.ClientConfiguration.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class RoomResourceFT {
    private static final String PREFIX = "11111111-2222-3333-4444-55555555";
    private static final UUID ROOM_ID_0 = UUID.fromString(PREFIX + "0000");
    private static final UUID UNKNOWN_ID = UUID.fromString(PREFIX + "9999");

    @Configuration
    @EnableAutoConfiguration
    @EnableFeignClients(clients = RoomClient.class)
    static class ClientConfiguration {
    }

    @Autowired
    private RoomClient client;

    @Test
    void testCreateUpdatePatchAndDelete() {
        Room newRoom = Room.builder()
                .name("Feign Test Room " + UUID.randomUUID())
                .capacity(25)
                .floor(2)
                .videoconferenceEquipped(true)
                .build();

        Room created = this.client.create(newRoom);
        assertThat(created.getId()).isNotNull();
        assertThat(created.getName()).isEqualTo(newRoom.getName());
        assertThat(created.getCapacity()).isEqualTo(25);
        assertThat(created.getFloor()).isEqualTo(2);
        assertThat(created.getVideoconferenceEquipped()).isTrue();
        assertThat(created.getCreatedAt()).isNotNull();

        created.setName("Feign Test Room Updated");
        created.setCapacity(50);
        created.setFloor(3);
        created.setVideoconferenceEquipped(false);
        Room updated = this.client.update(created.getId(), created);
        assertThat(updated).usingRecursiveComparison().ignoringFields("createdAt").isEqualTo(created);
        assertThat(updated.getCreatedAt()).isCloseTo(created.getCreatedAt(), within(1, MICROS));

        Room patchBody = Room.builder().name("Feign Test Room Patched").build();
        Room patched = this.client.patch(created.getId(), patchBody);
        assertThat(patched.getName()).isEqualTo("Feign Test Room Patched");
        assertThat(patched.getCapacity()).isEqualTo(50);

        this.client.delete(created.getId());
        assertThatThrownBy(() -> this.client.read(created.getId())).isInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void testRead() {
        Room room = this.client.read(ROOM_ID_0);
        assertThat(room.getId()).isEqualTo(ROOM_ID_0);
        assertThat(room.getName()).isEqualTo("Auditorium A");
        assertThat(room.getCapacity()).isEqualTo(100);
        assertThat(room.getFloor()).isEqualTo(1);
        assertThat(room.getVideoconferenceEquipped()).isTrue();
    }

    @Test
    void testFindAll() {
        assertThat(this.client.findAll()).extracting(Room::getName)
                .contains("Auditorium A", "Boardroom B", "Classroom C", "Meeting Room D");
    }

    @Test
    void testReadNotFound() {
        assertThatThrownBy(() -> this.client.read(UNKNOWN_ID)).isInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void testUpdateNotFound() {
        Room room = Room.builder().name("Unknown Room").capacity(10).floor(1).build();
        assertThatThrownBy(() -> this.client.update(UNKNOWN_ID, room)).isInstanceOf(FeignException.NotFound.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void testCreateInvalidName(String name) {
        Room room = Room.builder().name(name).capacity(10).floor(1).build();
        assertThatThrownBy(() -> this.client.create(room)).isInstanceOf(FeignException.BadRequest.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void testUpdateInvalidName(String name) {
        Room room = Room.builder().name(name).capacity(10).floor(1).build();
        assertThatThrownBy(() -> this.client.update(ROOM_ID_0, room)).isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testCreateWithoutCapacity() {
        Room room = Room.builder().name("No Capacity Room").floor(1).build();
        assertThatThrownBy(() -> this.client.create(room)).isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testCreateWithoutFloor() {
        Room room = Room.builder().name("No Floor Room").capacity(10).build();
        assertThatThrownBy(() -> this.client.create(room)).isInstanceOf(FeignException.BadRequest.class);
    }
}