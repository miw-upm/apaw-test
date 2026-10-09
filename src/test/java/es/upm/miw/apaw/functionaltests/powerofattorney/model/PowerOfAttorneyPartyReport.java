package es.upm.miw.apaw.functionaltests.powerofattorney.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PowerOfAttorneyPartyReport {
    private String userId;
    private long totalPowerOfAttorneysPresent;
    private long principalCount;
    private long attorneyCount;
}
