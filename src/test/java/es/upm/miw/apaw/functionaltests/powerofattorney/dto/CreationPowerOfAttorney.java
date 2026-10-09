package es.upm.miw.apaw.functionaltests.powerofattorney.dto;

import es.upm.miw.apaw.functionaltests.powerofattorney.model.PowerOfAttorneyStatus;
import es.upm.miw.apaw.functionaltests.powerofattorney.model.PowerOfAttorneyType;
import java.time.LocalDate;
import java.util.UUID;

public record CreationPowerOfAttorney(
        String protocolNumber, LocalDate grantDate, LocalDate expirationDate, String scope,
        String limitations, String notaryName, String notaryOffice, String notes,
        PowerOfAttorneyStatus status, PowerOfAttorneyType type, UUID principalId, UUID attorneyId) {
}
