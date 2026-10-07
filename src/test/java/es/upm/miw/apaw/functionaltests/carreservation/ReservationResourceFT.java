package es.upm.miw.apaw.functionaltests.carreservation;

import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = ReservationResourceFT.ClientConfiguration.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class ReservationResourceFT {

    private static final UUID CAR_ID_0 = UUID.fromString("11111111-2222-3333-4444-555566660000");
    private static final UUID USER_ID_1 = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0001");
    private static final UUID UNKNOWN_ID = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff9999");

    @Configuration
    @EnableAutoConfiguration
    @EnableFeignClients(clients = ReservationClient.class)
    static class ClientConfiguration {
    }

    @Autowired
    private ReservationClient reservationClient;

    private CreationReservation creation() {
        return CreationReservation.builder()
                .date(LocalDate.now().plusDays(5))
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(12, 0))
                .destination("Valencia")
                .businessTrip(true)
                .passengerCount(3)
                .carId(CAR_ID_0)
                .userId(USER_ID_1)
                .build();
    }

    @Test
    void testCreate() {
        CreationReservation creation = this.creation();
        creation.setDestination("Valencia " + UUID.randomUUID());

        Reservation actual = this.reservationClient.create(creation);
        assertThat(actual.getId()).isNotNull();
        assertThat(actual.getDestination()).isEqualTo(creation.getDestination());
        assertThat(actual.getCar()).isNotNull();
        assertThat(actual.getCar().getId()).isEqualTo(CAR_ID_0);
    }

    @Test
    void testCreateCarNotFound() {
        CreationReservation creation = this.creation();
        creation.setCarId(UNKNOWN_ID);

        assertThatThrownBy(() -> this.reservationClient.create(creation))
                .isInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void testCreateUserNotFound() {
        CreationReservation creation = this.creation();
        creation.setUserId(UNKNOWN_ID);

        assertThatThrownBy(() -> this.reservationClient.create(creation))
                .isInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void testCreateWithoutCar() {
        CreationReservation creation = this.creation();
        creation.setCarId(null);

        assertThatThrownBy(() -> this.reservationClient.create(creation))
                .isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testCreateWithoutUser() {
        CreationReservation creation = this.creation();
        creation.setUserId(null);

        assertThatThrownBy(() -> this.reservationClient.create(creation))
                .isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testFindCarUsageReport() {
        List<CarUsageReport> reports = this.reservationClient.findCarUsageReport();

        assertThat(reports).isNotNull().isNotEmpty();
        assertThat(reports)
                .extracting(CarUsageReport::getCarRegistration)
                .contains("1234BBB");
    }

    @Test
    void testFindByCriteria() {
        ReservationFindCriteria criteria = ReservationFindCriteria.builder()
                .durationMinutes(90)
                .carLicensePlate("1234BBB")
                .build();

        List<Reservation> reservations = this.reservationClient.find(criteria);

        assertThat(reservations).isNotNull().isNotEmpty();
        assertThat(reservations)
                .allSatisfy(r -> {
                    assertThat(r.getDurationMinutes()).isEqualTo(90);
                    assertThat(r.getCar()).isNotNull();
                    assertThat(r.getCar().getLicensePlate()).isEqualTo("1234BBB");
                });
    }

    @Test
    void testFindByCriteriaAllNullReturnsAll() {
        ReservationFindCriteria criteria = ReservationFindCriteria.builder().build();

        List<Reservation> reservations = this.reservationClient.find(criteria);

        assertThat(reservations).isNotNull().hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void testFindByCriteriaUnknownLicensePlateReturnsEmpty() {
        ReservationFindCriteria criteria = ReservationFindCriteria.builder()
                .carLicensePlate("9999ZZZ")
                .build();

        List<Reservation> reservations = this.reservationClient.find(criteria);

        assertThat(reservations).isNotNull().isEmpty();
    }
}