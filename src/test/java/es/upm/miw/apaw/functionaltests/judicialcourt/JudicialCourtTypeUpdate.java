package es.upm.miw.apaw.functionaltests.judicialcourt;

public record JudicialCourtTypeUpdate(
        String name,
        String description,
        String code,
        String jurisdiction,
        Boolean active
) {
}
