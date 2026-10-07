package es.upm.miw.apaw.functionaltests.carreservation;

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

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = CarResourceFT.ClientConfiguration.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class CarResourceFT {

    private static final UUID CAR_ID_0 = UUID.fromString("11111111-2222-3333-4444-555566660000");

    @Configuration
    @EnableAutoConfiguration
    @EnableFeignClients(clients = CarClient.class)
    static class ClientConfiguration {
    }

    @Autowired
    private CarClient carClient;

    @Test
    void testCreate() {
        Car carToCreate = Car.builder()
                .brand("Audi")
                .model("A3")
                .licensePlate("FT" + UUID.randomUUID().toString().substring(0, 5))
                .registrationDate(LocalDate.now())
                .numberOfSeats(5)
                .fuelType(FuelType.PETROL)
                .build();

        Car createdCar = this.carClient.create(carToCreate);

        assertThat(createdCar).isNotNull();
        assertThat(createdCar.getId()).isNotNull();
        assertThat(createdCar.getBrand()).isEqualTo(carToCreate.getBrand());
        assertThat(createdCar.getLicensePlate()).isEqualTo(carToCreate.getLicensePlate());
    }

    @Test
    void testCreateConflictAlreadyExists() {
        Car duplicateCar = Car.builder()
                .brand("Seat")
                .model("Ibiza")
                .licensePlate("1234BBB")
                .registrationDate(LocalDate.now())
                .numberOfSeats(5)
                .fuelType(FuelType.PETROL)
                .build();

        assertThatThrownBy(() -> this.carClient.create(duplicateCar))
                .isInstanceOf(FeignException.Conflict.class);
    }

    @Test
    void testRead() {
        Car car = this.carClient.read(CAR_ID_0);

        assertThat(car).isNotNull();
        assertThat(car.getId()).isEqualTo(CAR_ID_0);
        assertThat(car.getLicensePlate()).isEqualTo("1234BBB");
    }

    @Test
    void testReadNotFound() {
        UUID nonExistentId = UUID.randomUUID();

        assertThatThrownBy(() -> this.carClient.read(nonExistentId))
                .isInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void testUpdate() {
        Car carToUpdate = Car.builder()
                .brand("Seat")
                .model("Ibiza Updated")
                .licensePlate("1234BBB")
                .registrationDate(LocalDate.of(2021, 1, 15))
                .numberOfSeats(5)
                .fuelType(FuelType.PETROL)
                .build();

        Car updatedCar = this.carClient.update(CAR_ID_0, carToUpdate);

        assertThat(updatedCar).isNotNull();
        assertThat(updatedCar.getModel()).isEqualTo("Ibiza Updated");
    }

    @Test
    void testFindAll() {
        List<Car> cars = this.carClient.findAll();

        assertThat(cars).isNotNull()
                .extracting(Car::getLicensePlate)
                .contains("1234BBB", "5678CCC", "9012DDD", "3456EEE", "7890FFF");
    }

    @Test
    void testDeleteAndConflictIfInUse() {
        assertThatThrownBy(() -> this.carClient.delete(CAR_ID_0))
                .isInstanceOf(FeignException.Conflict.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void testCreateInvalidLicensePlate(String invalidPlate) {
        Car car = Car.builder()
                .brand("Audi")
                .model("A3")
                .licensePlate(invalidPlate)
                .registrationDate(LocalDate.now())
                .numberOfSeats(5)
                .fuelType(FuelType.PETROL)
                .build();

        assertThatThrownBy(() -> this.carClient.create(car))
                .isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testCreateUpdateAndDeleteHappyPath() {
        Car car = Car.builder()
                .brand("Audi")
                .model("A3")
                .licensePlate("NEW999")
                .registrationDate(LocalDate.now())
                .numberOfSeats(5)
                .fuelType(FuelType.PETROL)
                .build();

        Car created = this.carClient.create(car);
        assertThat(created.getId()).isNotNull();

        created.setModel("Modelo Nuevo");
        Car updated = this.carClient.update(created.getId(), created);
        assertThat(updated.getModel()).isEqualTo("Modelo Nuevo");

        this.carClient.delete(created.getId());

        assertThatThrownBy(() -> this.carClient.read(created.getId()))
                .isInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void testUpdateNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        Car carToUpdate = Car.builder()
                .brand("Seat")
                .model("Ibiza")
                .licensePlate("1234BBB")
                .registrationDate(LocalDate.now())
                .numberOfSeats(5)
                .fuelType(FuelType.PETROL)
                .build();

        assertThatThrownBy(() -> this.carClient.update(nonExistentId, carToUpdate))
                .isInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void testUpdateConflictAlreadyExists() {
        Car carToUpdate = Car.builder()
                .brand("Seat")
                .model("Ibiza")
                .licensePlate("5678CCC")
                .registrationDate(LocalDate.now())
                .numberOfSeats(5)
                .fuelType(FuelType.PETROL)
                .build();

        assertThatThrownBy(() -> this.carClient.update(CAR_ID_0, carToUpdate))
                .isInstanceOf(FeignException.Conflict.class);
    }
}