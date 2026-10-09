package es.upm.miw.apaw.functionaltests.powerofattorney.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PowerOfAttorney {
    private UUID id;
    private String protocolNumber;
    private LocalDate grantDate;
    private LocalDate expirationDate;
    private String scope;
    private String limitations;
    private String notaryName;
    private String notaryOffice;
    private String notes;
    private PowerOfAttorneyParty principal;
    private PowerOfAttorneyParty attorney;
    private PowerOfAttorneyType type;
    private PowerOfAttorneyStatus status;
}
