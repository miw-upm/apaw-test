package es.upm.miw.apaw.functionaltests.powerofattorney.dto;

import java.util.UUID;

public record PowerOfAttorneyPartyPatch(UUID id, Integer age, Boolean fullMentalCapacity) {
}
