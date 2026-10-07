package es.upm.miw.apaw.functionaltests.carreservation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationFindCriteria {
    private Integer durationMinutes;
    private Boolean active;
    private String carLicensePlate;
    private String userCity;
}