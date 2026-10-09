package es.upm.miw.apaw.functionaltests.powerofattorney.dto;

import java.util.UUID;

public record CreationPowerOfAttorneyParty(
        Integer age, Boolean fullMentalCapacity, String companyName,
        Boolean representationCompany, UUID userId) {
}
