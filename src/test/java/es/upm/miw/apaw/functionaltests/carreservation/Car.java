package es.upm.miw.apaw.functionaltests.carreservation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Car {
    private UUID id;
    private String brand;
    private String model;
    private String licensePlate;
    private LocalDate registrationDate;
    private Integer numberOfSeats;
    private FuelType fuelType;
}