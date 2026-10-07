package es.upm.miw.apaw.functionaltests.carreservation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarUsageReport {
    private String carRegistration;
    private UserSnapshot userSnapshot;
    private Long totalReservations;
    private Long totalDurationMinutes;
}