package es.upm.miw.apaw.functionaltests.carreservation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {
    private UUID id;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer durationMinutes;
    private String destination;
    private Boolean businessTrip;
    private Integer passengerCount;
    private UserSnapshot userSnapshot;
    private Car car;
}