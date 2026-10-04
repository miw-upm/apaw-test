package es.upm.miw.apaw.functionaltests.contract;

import java.time.LocalDate;

public record ClauseUpdate(
        ClauseType type,
        String notes,
        LocalDate effectiveUntil
) {

}