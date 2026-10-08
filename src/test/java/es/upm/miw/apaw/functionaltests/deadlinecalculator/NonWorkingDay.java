package es.upm.miw.apaw.functionaltests.deadlinecalculator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NonWorkingDay {
    private UUID id;
    private LocalDate date;
    private String description;
    private ScopeLevel scopeLevel;
    private String region;
    private String city;
    private Boolean recurring;
}
