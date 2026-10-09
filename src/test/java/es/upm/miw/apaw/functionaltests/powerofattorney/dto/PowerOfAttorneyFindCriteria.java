package es.upm.miw.apaw.functionaltests.powerofattorney.dto;

import es.upm.miw.apaw.functionaltests.powerofattorney.model.PowerOfAttorneyStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PowerOfAttorneyFindCriteria {
    private PowerOfAttorneyStatus status;
    private Boolean fullMentalCapacity;
    private Boolean legalPowerOfAttorney;
    private String identity;
}
