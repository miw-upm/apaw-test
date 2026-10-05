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

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = BookingResourceFT.ClientConfiguration.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class BookingResourceFT {
    private static final String PREFIX = "11111111-2222-3333-4444-55555555";
    private static final UUID USER_ID_0 = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0000");
    private static final UUID ROOM_ID_0 = UUID.fromString(PREFIX + "0000");
    private static final UUID ROOM_ID_1 = UUID.fromString(PREFIX + "0001");
    private static final UUID BOOKING_ID_0 = UUID.fromString(PREFIX + "1000");
    private static final UUID UNKNOWN_ID = UUID.fromString(PREFIX + "9999");

    @Configuration
    @EnableAutoConfiguration
    @EnableFeignClients(clients = BookingClient.class)
    static class ClientConfiguration {
    }

    @Autowired
    private BookingClient client;

    private CreationBooking creation() {
        return CreationBooking.builder()
                .name("Feign Booking " + UUID.randomUUID())
                .estimatedAttendees(15)
                .startDateTime(LocalDateTime.of(2026, 12, 1, 10, 0))
                .endDateTime(LocalDateTime.of(2026, 12, 1, 12, 0))
                .roomId(ROOM_ID_1)
                .userId(USER_ID_0)
                .build();
    }

    @Test
    void testCreate() {
        CreationBooking creation = this.creation();
        Booking actual = this.client.create(creation);

        assertThat(actual.getId()).isNotNull();
        assertThat(actual.getName()).isEqualTo(creation.getName());
        assertThat(actual.getEstimatedAttendees()).isEqualTo(creation.getEstimatedAttendees());
        assertThat(actual.getStartDateTime()).isEqualTo(creation.getStartDateTime());
        assertThat(actual.getEndDateTime()).isEqualTo(creation.getEndDateTime());
        assertThat(actual.getCreatedAt()).isNotNull();
        assertThat(actual.getRoom().getId()).isEqualTo(creation.getRoomId());
        assertThat(actual.getUserSnapshot().getId()).isEqualTo(creation.getUserId());
    }

    @Test
    void testFindAll() {
        assertThat(this.client.find(new BookingFindCriteria()))
                .extracting(Booking::getId)
                .contains(BOOKING_ID_0);
    }

    @Test
    void testFindByEstimatedAttendees() {
        assertThat(this.client.find(BookingFindCriteria.builder().estimatedAttendees(50).build()))
                .extracting(Booking::getId)
                .contains(BOOKING_ID_0);

        assertThat(this.client.find(BookingFindCriteria.builder().estimatedAttendees(1000).build()))
                .isEmpty();
    }

    @Test
    void testFindByVideoconferenceEquipped() {
        assertThat(this.client.find(BookingFindCriteria.builder().videoconferenceEquipped(true).build()))
                .extracting(Booking::getId)
                .contains(BOOKING_ID_0);
    }

    @Test
    void testFindUserBookingReports() {
        assertThat(this.client.findUserBookingReports())
                .filteredOn(report -> report.getUserSnapshot().getId().equals(USER_ID_0))
                .singleElement()
                .satisfies(report -> {
                    assertThat(report.getTotalBookings()).isGreaterThanOrEqualTo(1L);
                    assertThat(report.getTotalAttendees()).isGreaterThanOrEqualTo(50L);
                });
    }

    @Test
    void testCreateUnknownRoom() {
        CreationBooking creation = this.creation();
        creation.setRoomId(UNKNOWN_ID);
        assertThatThrownBy(() -> this.client.create(creation)).isInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void testCreateUnknownUser() {
        CreationBooking creation = this.creation();
        creation.setUserId(UNKNOWN_ID);
        assertThatThrownBy(() -> this.client.create(creation)).isInstanceOf(FeignException.NotFound.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void testCreateInvalidName(String name) {
        CreationBooking creation = this.creation();
        creation.setName(name);
        assertThatThrownBy(() -> this.client.create(creation)).isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testCreateWithoutEstimatedAttendees() {
        CreationBooking creation = this.creation();
        creation.setEstimatedAttendees(null);
        assertThatThrownBy(() -> this.client.create(creation)).isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testCreateWithoutStartDateTime() {
        CreationBooking creation = this.creation();
        creation.setStartDateTime(null);
        assertThatThrownBy(() -> this.client.create(creation)).isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testCreateWithoutEndDateTime() {
        CreationBooking creation = this.creation();
        creation.setEndDateTime(null);
        assertThatThrownBy(() -> this.client.create(creation)).isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testCreateWithoutRoom() {
        CreationBooking creation = this.creation();
        creation.setRoomId(null);
        assertThatThrownBy(() -> this.client.create(creation)).isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testCreateWithoutUser() {
        CreationBooking creation = this.creation();
        creation.setUserId(null);
        assertThatThrownBy(() -> this.client.create(creation)).isInstanceOf(FeignException.BadRequest.class);
    }
}